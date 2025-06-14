use thiserror::Error;

#[derive(Error, Debug)]
pub enum MitaError {
    #[error("config error")]
    Config(String),
    #[error("auth failed")]
    Auth,
    #[error("queue closed")]
    QueueClosed,
    #[error("network error: {0}")]
    Net(#[from] reqwest::Error),
}
