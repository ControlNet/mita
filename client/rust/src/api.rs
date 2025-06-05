use crate::error::MitaError;
use crate::MITA_VERSION;
use reqwest::blocking::Client;
use serde_json::json;
use std::cell::RefCell;

thread_local! {
    static TOKEN: RefCell<Option<String>> = const { RefCell::new(None) };
}

pub fn set_token(tok: String) {
    TOKEN.with(|t| *t.borrow_mut() = Some(tok));
}
pub fn get_token() -> Option<String> {
    TOKEN.with(|t| t.borrow().clone())
}

pub struct Api {
    url_base: String,
    http: Client,
}

impl Api {
    pub fn new(url_base: impl Into<String>) -> Self {
        Self {
            url_base: url_base.into(),
            http: Client::builder()
                .timeout(std::time::Duration::from_secs(30))
                .user_agent(format!("mita_client_rust/{}", MITA_VERSION))
                .build()
                .unwrap(),
        }
    }

    pub fn auth_token(&self, password: &str) -> Result<String, MitaError> {
        let resp = self
            .http
            .post(format!("{}/api/auth", self.url_base))
            .json(&json!({ "password": password }))
            .send()?;

        if resp.status().is_success() {
            let tok = resp
                .json::<serde_json::Value>()?["token"]
                .as_str()
                .ok_or(MitaError::Auth)?
                .to_owned();
            set_token(tok.clone());
            Ok(tok)
        } else {
            Err(MitaError::Auth)
        }
    }

    pub fn auth(&self, password: &str) -> Result<(), MitaError> {
        self.auth_token(password).map(|_| ())
    }

    pub fn push<T: serde::Serialize>(&self, payload: &T) -> Result<(), MitaError> {
        let tok = get_token().ok_or(MitaError::Auth)?;
        let resp = self
            .http
            .post(format!("{}/api/push", self.url_base))
            .header("X-Auth-Token", tok)
            .json(payload)
            .send()?;

        if resp.status().is_success() {
            Ok(())
        } else if resp.status().as_u16() == 401 {
            Err(MitaError::Auth)
        } else {
            Err(MitaError::Net(resp.error_for_status().unwrap_err()))
        }
    }
}

// Compatibility with Python CLI's state
pub enum State {
    Success,
    AuthError,
    ConnectionError,
}

impl From<Result<(), MitaError>> for State {
    fn from(r: Result<(), MitaError>) -> Self {
        match r {
            Ok(()) => State::Success,
            Err(MitaError::Auth) => State::AuthError,
            Err(_) => State::ConnectionError,
        }
    }
}
