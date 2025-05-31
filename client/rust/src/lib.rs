mod api;
mod worker;
mod client;
mod components;
mod error;
#[cfg(feature = "progress")]
mod mita_tqdm;
mod view;

pub use client::MitaClient as Mita;
pub use components::{
    line_chart::LineChart,
    logger::Logger,
    progress_bar::ProgressBar,
    variable::Variable,
};

pub use view::View;

/// similar to __version__ inside setup.py —— written to code
pub const MITA_VERSION: &str = env!("CARGO_PKG_VERSION");

#[cfg(feature = "progress")]
pub use mita_tqdm::MitaTqdm as MitaTqdm;
