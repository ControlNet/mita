use serde::Serialize;

#[derive(Serialize, Clone)]
pub struct Logger {
    name: String,
    value: Vec<String>,
}

impl Logger {
    pub fn new(name: impl Into<String>) -> Self {
        Self {
            name: name.into(),
            value: Vec::new(),
        }
    }
    pub fn log(&mut self, line: impl Into<String>) {
        self.value.push(line.into());
    }
}
