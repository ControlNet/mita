use serde::Serialize;

#[derive(Serialize, Clone)]
pub struct ProgressBar {
    name: String,
    value: f64,
    total: f64,
}

impl ProgressBar {
    pub fn new(name: impl Into<String>, value: f64, total: f64) -> Self {
        Self {
            name: name.into(),
            value,
            total,
        }
    }
    pub fn set(&mut self, value: f64) {
        self.value = value;
    }
}