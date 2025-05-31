#![cfg(feature = "progress")]

use indicatif::{ProgressBar as IndicatifBar, ProgressStyle};
use std::env;

use crate::{
    components::Component,
    view::View,
    client::MitaClient,
};
use crate::error::MitaError;

/// A wrapper around an `Iterator` that:
/// 1) shows an `indicatif` progress bar locally
/// 2) synchronises the same bar to a Mita server in the background.
///
/// ```no_run
/// use crate::mita::{MitaTqdm, View};
///
/// let data = 0..1_000;
/// for _ in MitaTqdm::new(data, None, None, None, false) {
///     std::thread::sleep(std::time::Duration::from_millis(50));
/// }
/// ```
pub struct MitaTqdm<I>
where
    I: Iterator,
{
    inner: I,
    bar: IndicatifBar,
    client: MitaClient,
    progress_comp: crate::components::progress_bar::ProgressBar,
    view_name: String,
    displayed_last: bool,
}

impl<I> MitaTqdm<I>
where
    I: Iterator,
{
    /// - iter: Iterable to decorate with a progressbar.
    /// - address (``str``, optional): The address of the Mita server. Default use ENV variable ``MITA_ADDRESS``.
    /// - password (``str``, optional): The password of the Mita server. Default use ENV variable ``MITA_PASSWORD``.
    /// - view (``View``, optional): The view used for mita client. If the input is `str`, it will be used as
    ///             the view name. Default name is the hostname.
    /// - verbose (``bool``): Whether to print debug information. Default ``false``.
    pub fn new(
        iter: I,
        address: Option<String>,
        password: Option<String>,
        view: Option<View>,
        verbose: bool,
    ) -> Result<Self,MitaError> {
        // ---------- resolve address/password ----------
        let address = address
            .or_else(|| env::var("MITA_ADDRESS").ok())
            .expect("address must be provided or set in $MITA_ADDRESS");
        let password = password
            .or_else(|| env::var("MITA_PASSWORD").ok())
            .expect("password must be provided or set in $MITA_PASSWORD");

        // ---------- indicatif bar ----------
        let upper = iter.size_hint().1.unwrap_or(0);
        let bar = IndicatifBar::new(upper as u64);
        bar.set_style(
            ProgressStyle::with_template("{desc:<12} {bar:40.cyan/blue} {pos}/{len} ({eta})")
                .unwrap(),
        );
        bar.set_message("Progress");

        // ---------- remote progress component ----------
        let progress =
            crate::components::progress_bar::ProgressBar::new(
                bar.message().to_string(), 0.0, upper as f64);

        // resolve view / view name
        let view_name = match &view {
            None => hostname::get()
                .unwrap_or_default()
                .to_string_lossy()
                .into_owned(),
            Some(v) => v.name().into(),
        };

        // -- create client (single worker, queue 64) and push initial bar --
        let client = MitaClient::new(
            Some(&address), Some(&password), Some(1), Some(64), verbose)?;
        // Push a temp view to refresh the server side model
        push_once(&client, &view_name, &progress).ok();

        Ok(Self {
            inner: iter,
            bar,
            client,
            progress_comp: progress,
            view_name,
            displayed_last: false,
        })
    }
}

impl<I> Iterator for MitaTqdm<I>
where
    I: Iterator,
{
    type Item = I::Item;

    fn next(&mut self) -> Option<Self::Item> {
        let next_item = self.inner.next();
        if next_item.is_some() {
            self.bar.inc(1);

            // Only push when bar changes
            let now = self.bar.position();
            if now != 0 || !self.displayed_last {
                self.progress_comp.set(now as f64);
                let _ = push_once(&self.client, &self.view_name, &self.progress_comp);
                self.displayed_last = true;
            }
        } else {
            // Stop iteration
            self.bar.finish();
            self.progress_comp.set(self.bar.length().unwrap_or(0) as f64);
            let _ = push_once(&self.client, &self.view_name, &self.progress_comp);
        }
        next_item
    }
}

/// helper：Construct a temporary View and push it immediately
fn push_once(
    cli: &MitaClient,
    view_name: &str,
    comp: &crate::components::progress_bar::ProgressBar,
) -> Result<(), crate::error::MitaError> {
    let mut v = View::new(Some(view_name.to_owned()));
    v.add([Component::from((*comp).clone())]);
    cli.add(&v)
}
