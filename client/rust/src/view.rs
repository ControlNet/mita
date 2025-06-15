use crate::components::Component;
use serde::Serialize; // assumes serde + derive is enabled in Cargo.toml
use std::iter::IntoIterator;

#[derive(Serialize)]
pub struct View {
    view: String,
    data: Vec<Component>,
}

impl View {
    pub fn new(name: Option<impl Into<String>>) -> Self {
        let view = name
            .map(Into::into)
            .unwrap_or_else(|| hostname::get().unwrap_or_default().to_string_lossy().into());
        Self {
            view,
            data: Vec::new(),
        }
    }

    /// Push several components at once and return a chainable &mut Self
    pub fn add<I>(&mut self, components: I) -> &mut Self
    where
        I: IntoIterator<Item = Component>,
    {
        self.data.extend(components);
        self
    }

    /// Serialize to the equivalent Python dict structure
    pub fn to_json(&self) -> serde_json::Value {
        serde_json::json!({
            "view": self.view,
            "data": self.data,   // each component already implements Serialize
        })
    }

    pub fn name(&self) -> &str {
        &self.view
    }
}
