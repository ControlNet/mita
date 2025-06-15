use chrono::Utc;
use clap::{Args, Parser, Subcommand, ValueEnum};
use mita::jwt::{parse_jwt_claims, JwtClaims};
use mita::spinner_utils::with_spinner;
use mita::token_store;
use mita::{Api, Component, LineChart, Logger, MitaError, ProgressBar, Variable, View};

#[derive(Parser)]
#[command(name = "mita", about = "CLI for Mita Rust client")]
struct Cli {
    #[command(subcommand)]
    command: Commands,
}

#[derive(Subcommand)]
enum Commands {
    /// Authenticate and store token
    Auth(AuthOpts),
    /// Push a single component update
    Push(PushOpts),
}

#[derive(Args)]
struct AuthOpts {
    #[arg(long)]
    url: Option<String>,
    #[arg(long)]
    password: Option<String>,
    #[arg(long, help = "Force re-authentication even if token exists")]
    force: bool,
}

#[derive(Clone, Debug, ValueEnum)]
#[clap(rename_all = "snake_case")]
enum ComponentType {
    Variable,
    ProgressBar,
    Logger,
    LineChart,
}

#[derive(Args)]
struct PushOpts {
    #[arg(long)]
    view: Option<String>,
    #[arg(long)]
    url: Option<String>,
    #[arg(long)]
    password: Option<String>,
    #[arg(value_enum)]
    component_type: ComponentType,
    component_name: String,
    component_value: String,
    #[arg(long)]
    total: Option<f64>,
}

fn main() {
    let cli = Cli::parse();
    match cli.command {
        Commands::Auth(opts) => cmd_auth(opts),
        Commands::Push(opts) => cmd_push(opts),
    }
}

fn resolve_url(arg: Option<String>) -> String {
    arg.or_else(|| std::env::var("MITA_ADDRESS").ok())
        .or_else(|| token_store::load_token_store().last_url)
        .expect("MITA_ADDRESS not set (no CLI arg, no env, no auth token)")
}

fn resolve_pwd(arg: Option<String>) -> String {
    arg.or_else(|| std::env::var("MITA_PASSWORD").ok())
        .expect("MITA_PASSWORD not set")
}

fn resolve_token(url: &str) -> Option<String> {
    token_store::load_token_store().tokens.get(url).cloned()
}

fn print_token_info(url: &String, claims: &JwtClaims) {
    println!("🔐 Already authenticated to: {url}, using auth token of last connection ...");

    if let Some(exp_time) = claims.get_expire_datetime() {
        println!(
            "📅 Token valid through: {}",
            exp_time.format("%Y-%m-%d %H:%M:%S")
        );
        if exp_time < Utc::now() {
            println!("❌ Token is expired now.");
            println!("💡 Please execute: mita auth --force");
            return;
        }
    } else {
        println!("⚠️ No expire date found.");
    }

    if let Some(iss) = &claims.iss {
        println!("🔖 Issuer: {iss}.");
    }

}

fn cmd_auth(opts: AuthOpts) {
    let url = resolve_url(opts.url);

    let mut store = token_store::load_token_store();

    if !opts.force {
        if let Some(tok) = store.tokens.get(&url) {
            let Some(claims) = parse_jwt_claims(tok) else {
                eprintln!("[Mita/Auth] Error: cannot read JWT claims");
                return;
            };

            print_token_info(&url, &claims);

            if !claims.is_token_expired() {
                // Silently exit the program
                return;
            }

            println!("⚠️ Token expired, trying to refresh it...");
        }
    }

    let password = resolve_pwd(opts.password);
    let api = Api::new(&url);

    let auth_result = with_spinner(&format!(" Authenticating to {url}..."), || {
        api.auth_token(&password)
    });

    match auth_result {
        Ok(tok) => {
            store.insert_new_token(&url, tok);
            token_store::save_token_store(&store);
            println!("✅ Auth success to server: {url}");
        }
        Err(e) => {
            eprintln!("[Mita/Auth] Auth failed: {e}");
            std::process::exit(1);
        }
    }
}

fn cmd_push(opts: PushOpts) {
    let url = resolve_url(opts.url.clone());
    let api = Api::new(&url);
    if let Some(tok) = resolve_token(&url) {
        mita::api::set_token(tok.clone());
    }

    let view_name = opts.view.unwrap_or_else(|| {
        hostname::get()
            .map(|h| h.to_string_lossy().into_owned())
            .unwrap_or_else(|_| "default".into())
    });

    let comp = match opts.component_type {
        ComponentType::Variable => {
            let v = Variable::new(opts.component_name, opts.component_value);
            Component::from(v)
        }
        ComponentType::ProgressBar => {
            let total = opts.total.unwrap_or(100.0);
            let val: f64 = opts.component_value.parse().unwrap_or(0.0);
            let pb = ProgressBar::new(opts.component_name, val, total);
            Component::from(pb)
        }
        ComponentType::Logger => {
            let mut lg = Logger::new(opts.component_name);
            lg.log(opts.component_value);
            Component::from(lg)
        }
        ComponentType::LineChart => {
            let mut lc = LineChart::new(opts.component_name, "x", "y");
            // value expected as "x,y,label"
            let parts: Vec<&str> = opts.component_value.split(',').collect();
            if parts.len() == 3 {
                if let (Ok(x), Ok(y)) = (parts[0].parse::<f64>(), parts[1].parse::<f64>()) {
                    lc.add(x, y, parts[2]);
                }
            }
            Component::from(lc)
        }
    };

    let mut view = View::new(Some(view_name));
    view.add([comp]);

    let push_result = with_spinner(" Sending push payload...", || api.push(&view));

    match push_result {
        Ok(()) => {
            println!("📤 Push success.");
            return;
        }
        Err(MitaError::Auth) => {
            if let Some(pwd) = opts
                .password
                .or_else(|| std::env::var("MITA_PASSWORD").ok())
            {
                match api.auth_token(&pwd) {
                    Ok(tok) => {
                        let mut token_store = token_store::load_token_store();
                        token_store.last_url = Some(url.clone());
                        token_store.tokens.insert(url.clone(), tok.clone());
                        token_store::save_token_store(&token_store);
                        if api.push(&view).is_ok() {
                            println!("📤 Push success.");
                            return;
                        }
                    }
                    Err(e) => {
                        eprintln!("[Mita/Auth] Auth failed: {e}");
                        std::process::exit(1);
                    }
                }
            }
            eprintln!("[Mita/Auth] Authentication required");
            std::process::exit(1);
        }
        Err(e) => {
            eprintln!("[Mita/Error] Push failed: {e}");
            std::process::exit(1);
        }
    }
}
