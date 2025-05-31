mod api;
mod worker;
mod client;
mod components;
mod error;
mod mita_tqdm;
mod view;

pub use client::MitaClient as Mita;
pub use components::variable::Variable;
pub use components::progress_bar::ProgressBar;
pub use components::logger::Logger;
pub use components::line_chart::LineChart;
pub use view::View;

/// similar to __version__ inside setup.py —— written to code
pub const MITA_VERSION: &str = env!("CARGO_PKG_VERSION");
