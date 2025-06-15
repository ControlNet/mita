use std::time::Duration;

pub fn with_spinner<T, F: FnOnce() -> T>(msg: &str, f: F) -> T {
    #[cfg(feature = "progress")]
    {
        use indicatif;
        let spinner = indicatif::ProgressBar::new_spinner();
        spinner.set_style(
            indicatif::ProgressStyle::with_template("{spinner} {msg}")
                .unwrap()
                .tick_strings(&["⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"]),
        );
        spinner.set_message(msg.to_string());
        spinner.enable_steady_tick(Duration::from_micros(100));

        let result = f();
        spinner.finish_and_clear();
        result
    }

    #[cfg(not(feature = "progress"))]
    {
        use crate::MAX_WAITING_SEC;
        println!("{msg} (max wait ~{MAX_WAITING_SEC}s)");
        f()
    }
}
