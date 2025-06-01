//! client.rs —— End-user API that only exposes useful methods
use crate::{
    error::MitaError,
    view::View, // View, derived from Serialize
    worker::{MitaWorker, Payload},
};
use serde_json::to_value;

/// `MitaClient` uses `MitaWorker` to do network IO but it does not perform it per se
pub struct MitaClient {
    // Option, sot that it would be easy to be removed from `Drop` by `take()`
    worker: Option<MitaWorker>,
}

impl MitaClient {
    /// `url` = the url to connect to Mita Server, will retrieve env var `MITA_ADDRESS` if absent
    /// `password` = the url to connect to Mita Server, will retrieve env var `MITA_PASSWORD` if absent
    /// `threads` = number of thread to push at background, default is 1
    /// `queue_cap` = capacity of the queue, default is 256
    /// `verbose` = should output verbose log, default is false
    pub fn new(
        url: Option<impl Into<String>>,
        password: Option<impl Into<String>>,
        threads: Option<usize>,   // 默认为 1
        queue_cap: Option<usize>, // 默认为 256
        verbose: bool,            // 和 Python 一致：默认 false
    ) -> Result<Self, MitaError> {
        // ---------- 1. Parse URL / PASSWORD ----------
        let url = url
            .map(Into::into)
            .or_else(|| std::env::var("MITA_ADDRESS").ok())
            .ok_or_else(|| MitaError::Config("MITA_ADDRESS not set".into()))?;

        let password = password
            .map(Into::into)
            .or_else(|| std::env::var("MITA_PASSWORD").ok())
            .ok_or_else(|| MitaError::Config("MITA_PASSWORD not set".into()))?;

        // ---------- 2. Parse other params ----------
        let threads = threads.unwrap_or(1);
        let queue_cap = queue_cap.unwrap_or(256);

        // ---------- 3. Launch worker ----------
        let worker = MitaWorker::new(url, password, threads, queue_cap, verbose);
        Ok(Self {
            worker: Some(worker),
        })
    }

    pub fn init(
        url: impl Into<String>,
        password: impl Into<String>,
        verbose: bool,
    ) -> Result<Self, MitaError> {
        Self::new(Some(url), Some(password), None, None, verbose)
    }

    /// Add view into the sending query
    pub fn add(&self, view: &View) -> Result<(), MitaError> {
        let payload: Payload = to_value(view).expect("Error: view must be serializable");
        self.worker
            .as_ref()
            .expect("Error: worker already dropped")
            .submit(payload)
            .map_err(|_| MitaError::QueueClosed)
    }

    /// Wait explicitly for all background tasks to end (optional)
    pub fn join(&mut self) {
        if let Some(w) = self.worker.take() {
            w.stop();
            w.join();
        }
    }
}

impl Drop for MitaClient {
    fn drop(&mut self) {
        self.join(); // Prevent to leave dangling threads
    }
}
