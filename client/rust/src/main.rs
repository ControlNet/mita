use std::thread::sleep;
use std::time;
use mita::{LineChart, Logger, Mita, ProgressBar, Variable, View};
use mita::Component;

const ADDRESS: &str = env!("MITA_ADDRESS");
const PASSWORD: &str = env!("MITA_PASSWORD");

fn main() -> Result<(), Box<dyn std::error::Error>> {

    let mut view = View::new(Some("rust_view"));
    let mut logger = Logger::new("rust_logger");
    let mut line_chart = LineChart::new("rust_line_chart", "x", "y");
    let mut progress_bar = ProgressBar::new("rust_progress_bar", 0.0, 100.0);
    let mut var = Variable::new("rust_var", 0.0);

    let components: Vec<Component> = vec![
        var.clone().into(), progress_bar.clone().into(), logger.clone().into(), line_chart.clone().into()
    ];

    view.add(components);


    let client = Mita::init(ADDRESS, PASSWORD, true)?;

    client.add(&view)?;

    for i in 0..10 {
        sleep(time::Duration::from_millis(1000));

        logger.log(&format!("some msg {}", i));
        line_chart.add(1.0 + i as f64, 1.0 + i as f64, "pos");
        line_chart.add(1.0 + i as f64, 3.5 - i as f64, "neg");
        progress_bar.set(i as f64 * 8.0 + 1.0);
        var.set(i as f64 * 10.0);


        let current_components: Vec<Component> = vec![
            var.clone().into(), progress_bar.clone().into(), logger.clone().into(), line_chart.clone().into()
        ];

        view.add(current_components);

        client.add(&view)?;
    }

    println!("Process finished.");

    Ok(())
}

