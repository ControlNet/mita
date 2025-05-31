use serde::Serialize;   // 假设已在 Cargo.toml 启用 serde + derive
use std::iter::IntoIterator;
use crate::components::Component;

#[derive(Serialize)]
pub struct View {
    name: String,
    data: Vec<Component>,
}

impl View {
    pub fn new(name: Option<impl Into<String>>) -> Self {
        let name = name
            .map(Into::into)
            .unwrap_or_else(|| {
                hostname::get().unwrap_or_default().to_string_lossy().into()
            });
        Self {
            name,
            data: Vec::new(),
        }
    }

    /// 把若干组件批量压入并返回可链式调用的 &mut Self
    pub fn add<I>(&mut self, components: I) -> &mut Self
    where
        I: IntoIterator<Item = Component>,
    {
        self.data.extend(components);
        self
    }

    /// 序列化成等价的 Python dict 结构
    pub fn to_json(&self) -> serde_json::Value {
        serde_json::json!({
            "view": self.name,
            "data": self.data,   // 组件本身已实现 Serialize
        })
    }
}
