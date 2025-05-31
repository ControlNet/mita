//! worker.rs —— The background thread pool which is responsible to push JSON views to Mita Server
use crossbeam_channel::{bounded, Receiver, Sender};
use std::{
    sync::{
        atomic::{AtomicBool, Ordering},
        Arc,
    },
    thread::{self, JoinHandle},
    time::Instant,
};

use crate::{api::Api, error::MitaError};

pub type Payload = serde_json::Value;

/// Background pusher: it maintains its own API value and query
pub struct MitaWorker {
    tx: Sender<Payload>,
    handles: Vec<JoinHandle<()>>,
    stop_flag: Arc<AtomicBool>,
}

impl MitaWorker {
    /// Start and launch a few threads
    pub fn new(
        api_url: impl Into<String>,
        password: impl Into<String>,
        threads: usize,
        queue_cap: usize,
        verbose: bool,
    ) -> Self {
        let (tx, rx) = bounded::<Payload>(queue_cap);
        let url = api_url.into();
        let pwd = password.into();
        let stop_flag = Arc::new(AtomicBool::new(false));

        let mut handles = Vec::with_capacity(threads);
        for _ in 0..threads {
            handles.push(Self::spawn_one(
                rx.clone(),
                stop_flag.clone(),
                url.clone(),
                pwd.clone(),
                verbose,
            ));
        }

        Self {
            tx,
            handles,
            stop_flag,
        }
    }

    /// Producer API: put the serialized view into query
    pub fn submit(&self, payload: Payload) -> Result<(), crossbeam_channel::SendError<Payload>> {
        self.tx.send(payload)
    }

    /// Send the `STOP` signal (whilst it's still possible to further call `MitaWorker::join` to
    /// wait for the threads to leave)
    pub fn stop(&self) {
        self.stop_flag.store(true, Ordering::SeqCst);
        drop(self.tx.clone()); // Close all senders and make recv to return Err immediately
    }

    /// Blocking to wait for all background threads to leave
    pub fn join(self) {
        for h in self.handles {
            let _ = h.join();
        }
    }

    /* ---------- 内部 ---------- */

    fn spawn_one(
        rx: Receiver<Payload>,
        stop: Arc<AtomicBool>,
        url: String,
        password: String,
        verbose: bool,
    ) -> JoinHandle<()> {
        thread::spawn(move || {
            let api = Api::new(url);

            // First login (Blocking retries)
            while api.auth(&password).is_err() {
                eprintln!("[MitaWorker] auth failed, retry in 3s");
                thread::sleep(std::time::Duration::from_secs(3));
            }

            // Main loop
            while !stop.load(Ordering::Relaxed) {
                match rx.recv() {
                    Ok(payload) => {
                        let t0 = Instant::now();
                        match api.push(&payload) {
                            Ok(()) => {
                                if verbose {
                                    println!(
                                        "[MitaWorker] pushed in {:.3}s",
                                        t0.elapsed().as_secs_f64()
                                    );
                                }
                            }
                            Err(MitaError::Auth) => {
                                if api.auth(&password).is_ok() {
                                    let _ = api.push(&payload);
                                } else {
                                    eprintln!("[MitaWorker] auth retry failed");
                                }
                            }
                            Err(MitaError::Net(e)) => {
                                eprintln!("[MitaWorker] connection error: {e}");
                            }
                            Err(MitaError::Config(reason)) => {
                                eprintln!("[MitaWorker] config error: {reason}");
                            }
                            Err(MitaError::QueueClosed) => {
                                eprintln!("[MitaWorker] queue closed");
                            }
                        }
                    }
                    Err(_) => break, // All senders are closed, exit
                }
            }
        })
    }
}
