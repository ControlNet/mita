use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use std::fs;
use std::path::PathBuf;

#[derive(Serialize, Deserialize, Debug, Default)]
pub struct TokenStore {
    pub last_url: Option<String>,
    pub tokens: HashMap<String, String>,
}

impl TokenStore {
    pub fn insert_new_token(&mut self, url: &str, token: String) {
        self.tokens.insert(url.to_string(), token);
        self.last_url = Some(url.to_string());
    }
}

fn token_file() -> PathBuf {
    let base = match dirs::home_dir() {
        Some(dir) => dir,
        None => {
            eprintln!("[Mita] Home directory not found, using current directory for token file");
            PathBuf::from(".")
        }
    };
    base.join(".mita.json")
}

pub fn load_token_store() -> TokenStore {
    let path = token_file();
    if let Ok(s) = fs::read_to_string(path) {
        match serde_json::from_str(&s) {
            Ok(store) => store,
            Err(e) => {
                eprintln!("[Mita] Failed to deserialize token file: {e}");
                TokenStore::default()
            }
        }
    } else {
        TokenStore::default()
    }
}

pub fn save_token_store(store: &TokenStore) {
    let path = token_file();
    match serde_json::to_string_pretty(store) {
        Ok(json) => {
            if let Err(e) = fs::write(&path, json) {
                eprintln!("[Mita] Failed to write token file: {e}");
            }
        }
        Err(e) => {
            eprintln!("[Mita] Failed to serialize token store: {e}");
        }
    }
}
