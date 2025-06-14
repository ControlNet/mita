use clap::{Parser, Subcommand, Args, ValueEnum};
use std::collections::HashMap;
use std::fs;
use std::path::PathBuf;
use serde::{Deserialize, Serialize};
use mita::{Api, View, Variable, ProgressBar, Logger, LineChart, Component, MitaError};

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

#[derive(Serialize, Deserialize, Debug, Default)]
struct TokenStore {
    pub last_url: Option<String>,
    pub tokens: HashMap<String, String>,
}

fn token_file() -> PathBuf {
    let base = match dirs::home_dir() {
        Some(dir) => dir,
        None => {
            eprintln!("Home directory not found, using current directory for token file");
            PathBuf::from(".")
        }
    };
    base.join(".mita.json")
}

fn load_token_store() -> TokenStore {
    let path = token_file();
    if let Ok(s) = fs::read_to_string(path) {
        serde_json::from_str(&s).unwrap_or_default()
    } else {
        TokenStore::default()
    }
}

fn save_token_store(store: &TokenStore) {
    let path = token_file();
    match serde_json::to_string_pretty(store) {
        Ok(json) => {
            if let Err(e) = fs::write(&path, json) {
                eprintln!("Failed to write token file: {e}");
            }
        }
        Err(e) => {
            eprintln!("Failed to serialize token store: {e}");
        }
    }
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
        .or_else(|| { load_token_store().last_url })
        .expect("MITA_ADDRESS not set (no CLI arg, no env, no auth token)")
}

fn resolve_pwd(arg: Option<String>) -> String {
    arg.or_else(|| std::env::var("MITA_PASSWORD").ok())
        .expect("MITA_PASSWORD not set")
}

fn resolve_token(url: &str) -> Option<String> {
    load_token_store().tokens.get(url).cloned()
}

fn cmd_auth(opts: AuthOpts) {
    let url = resolve_url(opts.url);

    let mut store = load_token_store();

    if !opts.force {
        if let Some(tok) = store.tokens.get(&url) {
            println!("Already authenticated: {url}");
            return;
        }
    }

    println!("Authenticating into: {url}");

    let password = resolve_pwd(opts.password);
    let api = Api::new(&url);
    match api.auth_token(&password) {
        Ok(tok) => {
            store.last_url = Some(url.clone());
            store.tokens.insert(url.clone(), tok.clone());
            save_token_store(&store);
            println!("Auth success into: {url}");
        }
        Err(e) => {
            eprintln!("Auth failed: {e}");
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

    match api.push(&view) {
        Ok(()) => {
            println!("Push success");
            return;
        }
        Err(MitaError::Auth) => {
            if let Some(pwd) = opts.password.or_else(|| std::env::var("MITA_PASSWORD").ok()) {
                match api.auth_token(&pwd) {
                    Ok(tok) => {
                        let mut token_store = load_token_store();
                        token_store.last_url = Some(url.clone());
                        token_store.tokens.insert(url.clone(), tok.clone());
                        save_token_store(&token_store);
                        if api.push(&view).is_ok() {
                            println!("Push success");
                            return;
                        }
                    }
                    Err(e) => {
                        eprintln!("Auth failed: {e}");
                        std::process::exit(1);
                    }
                }
            }
            eprintln!("Authentication required");
            std::process::exit(1);
        }
        Err(e) => {
            eprintln!("Push failed: {e}");
            std::process::exit(1);
        }
    }
}
