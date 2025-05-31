use serde::Serialize;

#[derive(Serialize)]
pub struct Variable {
    name: String,
    value: serde_json::Value,            // 任意 JSON，可装 int/float/str…
}

impl Variable {
    pub fn new(name: impl Into<String>, value: impl Serialize) -> Self {
        Self {
            name: name.into(),
            value: serde_json::to_value(value).unwrap(),
        }
    }
    pub fn set(&mut self, value: impl Serialize) {
        self.value = serde_json::to_value(value).unwrap();
    }
}