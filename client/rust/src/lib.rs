pub mod api;
mod client;
mod components;
mod error;
pub mod jwt;
#[cfg(feature = "progress")]
mod mita_tqdm;
pub mod spinner_utils;
pub mod token_store;
mod view;
mod worker;

pub use client::MitaClient as Mita;
pub use components::{
    line_chart::LineChart, logger::Logger, progress_bar::ProgressBar, variable::Variable,
};

pub use components::common::Component;

pub use view::View;

/// similar to __version__ inside setup.py —— written to code
pub const MITA_VERSION: &str = env!("CARGO_PKG_VERSION");

pub const MAX_WAITING_SEC: u64 = 30;

#[cfg(feature = "progress")]
pub use mita_tqdm::MitaTqdm;

pub use api::{Api, State};
pub use error::MitaError;
