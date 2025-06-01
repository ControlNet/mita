use serde::Serialize;

#[derive(Serialize, Clone)]
pub struct Point {
    x: f64,
    y: f64,
    label: String,
}

#[derive(Serialize, Clone)]
pub struct LineChart {
    name: String,
    value: Vec<Point>,
    x_label: String,
    y_label: String,
}

impl LineChart {
    pub fn new(
        name: impl Into<String>,
        x_label: impl Into<String>,
        y_label: impl Into<String>,
    ) -> Self {
        Self {
            name: name.into(),
            value: Vec::new(),
            x_label: x_label.into(),
            y_label: y_label.into(),
        }
    }
    pub fn add(&mut self, x: f64, y: f64, label: impl Into<String>) {
        self.value.push(Point {
            x,
            y,
            label: label.into(),
        });
    }
}
