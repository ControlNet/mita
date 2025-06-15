use base64::{engine::general_purpose::URL_SAFE_NO_PAD, Engine};
use chrono::{DateTime, Utc};
use serde::Deserialize;

#[derive(Debug, Deserialize)]
pub struct JwtClaims {
    pub iss: Option<String>,
    pub exp: Option<i64>,
    iat: Option<i64>,
    sub: Option<String>,
    jti: Option<String>,
}

impl JwtClaims {
    pub fn get_expire_datetime(&self) -> Option<DateTime<Utc>> {
        self.exp
            .and_then(|ts| DateTime::<Utc>::from_timestamp(ts, 0))
    }

    pub fn is_token_expired(&self) -> bool {
        self.get_expire_datetime()
            .map(|dt| dt < Utc::now())
            .unwrap_or(true)
    }
}

pub fn parse_jwt_claims(token: &str) -> Option<JwtClaims> {
    let payload = token.split('.').nth(1)?;
    let decoded = URL_SAFE_NO_PAD.decode(payload).ok()?;
    serde_json::from_slice(&decoded).ok()
}
