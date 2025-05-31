use serde::Serialize;
use super::line_chart::LineChart;
use super::logger::Logger;
use super::progress_bar::ProgressBar;
use super::variable::Variable;

#[derive(Serialize)]
#[serde(tag = "cls")]                 // Generate structs like {"cls":"Variable", ...}
pub enum Component {
    Variable(Variable),
    ProgressBar(ProgressBar),
    Logger(Logger),
    LineChart(LineChart),
}

// Helper method to wrap `.add()`
impl From<Variable> for Component {
    fn from(v: Variable) -> Self { Component::Variable(v) }
}
impl From<ProgressBar> for Component {
    fn from(v: ProgressBar) -> Self { Component::ProgressBar(v) }
}
impl From<Logger> for Component {
    fn from(v: Logger) -> Self { Component::Logger(v) }
}
impl From<LineChart> for Component {
    fn from(v: LineChart) -> Self { Component::LineChart(v) }
}