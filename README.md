# Mita

<div align="center">
    <img src="https://img.shields.io/github/stars/ControlNet/mita?style=flat-square">
    <img src="https://img.shields.io/github/forks/ControlNet/mita?style=flat-square">
    <a href="https://github.com/ControlNet/mita/issues"><img src="https://img.shields.io/github/issues/ControlNet/mita?style=flat-square"></a>
    <img src="https://img.shields.io/github/license/ControlNet/mita?style=flat-square">
</div>

<div align="center">
    <a href="https://hub.docker.com/r/controlnet/mita">
        <img src="https://img.shields.io/docker/image-size/controlnet/mita?style=flat-square&logo=docker&label=Docker">
    </a>
    <a href="https://pypi.org/project/mita_client/">
        <img src="https://img.shields.io/pypi/v/mita_client?style=flat-square&logo=python&label=Python">
    </a>
    <a href="https://crates.io/crates/mita">
        <img src="https://img.shields.io/crates/v/mita?style=flat-square&logo=rust&label=Rust">
    </a>
</div>

Mita, named as Multi-Tangled, is a web application for monitoring runtime status and data of multiple machines 
(clients).

Demo: [https://mita-demo.controlnet.space](https://mita-demo.controlnet.space)

<img src=".github/demo.png" style="max-height: 480px">

## Get Started

### Run Server

The server is used to accept client post data and host the web UI.

#### Docker
```bash
docker run \
  -d \
  -p <PORT>:9000 \
  -e MITA_PASSWORD=<PASSWORD> \
  [-e MITA_GUEST_PASSWORD=<GUEST_PASSWORD>] \
  -v <DATA_DIR>:/app/data \
  --name mita \
  --restart=unless-stopped \
  controlnet/mita[:<VERSION>]
```

#### Environment Variables

- `MITA_PASSWORD`: Password for **admin** accessing the api and web for read/write.
- `MITA_GUEST_PASSWORD`: Password for **guest** accessing the api and web for read only.
- `REACT_APP_PASSWORD`: _Optional_, set the default password for the frontend.
- `MITA_SECRET_KEY`: _Optional_, Server secret key for CRSF token.
- `MITA_TOKEN_SECRET`: _Optional_, Server secret key for JWT authentication token.

### Run Client

The client is the interface to post data to the server.

A Python client and a Rust CLI are included in this repository.

#### Python Client

Install the client from pypi:
```bash
pip install mita_client
```

Use the tqdm integrated client (require `tqdm` for progress bar):
```python
from mita_client import mita_tqdm
import time

for i in mita_tqdm(range(1000), ADDRESS, PASSWORD):
    time.sleep(0.1)
```

Use the fully client:
```python
from mita_client.client import Mita
from mita_client.component import *
from mita_client.view import View

# initialize components
view = View("python_view")
logger = Logger("python_logger")
line_chart = LineChart("python_line_chart")
progress_bar = ProgressBar("python_progress_bar", total=100)
var = Variable("python_var", 100)

# register components to view
view.add(
    var,
    progress_bar,
    logger,
    line_chart
)

# update data in the runtime, and post to the client
with Mita(ADDRESS, PASSWORD) as client:
    client.add(view)

    for i in range(10):
        logger.log(f"some msg {i}")
        line_chart.add(1 + i, 1 + i, "pos")
        line_chart.add(1 + i, 3.5 - i, "neg")
        progress_bar.set(i * 8 + 1)
        
        client.push()
```

#### CLI App (Rust-based)

Build the Rust CLI from source:

```bash
cargo install mita
```

Authenticate with a Mita server (falls back to `MITA_ADDRESS` and `MITA_PASSWORD` when flags are omitted):

```bash
mita auth --url http://your.mita.server:9000 --password <PASSWORD>
```

Tokens are stored in `~/.mita.json` per server URL.

Push updates (falls back to `MITA_ADDRESS` and `MITA_PASSWORD` when flags are omitted):

```bash
mita push [--view <VIEW_NAME>] <COMPONENT_TYPE> <COMPONENT_NAME> <COMPONENT_VALUE> [--args]
# examples
mita push progress_bar progress 20 --total 100
mita push --view my_view variable some_var "Hello World"
```

#### Rust Client Library

Add the crate to your `Cargo.toml`:

```toml
# enable "progress" and "log" features if needed
mita = { version = "0.0.0", features = ["progress", "log"] } # check crates.io for the latest version
```

Use the library in your Rust application:

```rust
use mita::{Mita, View, Variable, ProgressBar, Logger, LineChart, Component};

fn main() -> Result<(), Box<dyn std::error::Error>> {
    let mut logger = Logger::new("rust_logger");
    let mut line_chart = LineChart::new("rust_line_chart", "x", "y");
    let mut progress_bar = ProgressBar::new("rust_progress_bar", 0.0, 100.0);
    let var = Variable::new("rust_var", 100);

    let mut client = Mita::init(ADDRESS, PASSWORD, false)?;

    for i in 0..10 {
        logger.log(format!("some msg {i}"));
        line_chart.add(1.0 + i as f64, 1.0 + i as f64, "pos");
        line_chart.add(1.0 + i as f64, 3.5 - i as f64, "neg");
        progress_bar.set(i as f64 * 8.0 + 1.0);

        let mut view = View::new(Some("rust_view"));
        view.add([
            Component::from(var.clone()),
            Component::from(progress_bar.clone()),
            Component::from(logger.clone()),
            Component::from(line_chart.clone()),
        ]);

        client.add(&view)?;
    }

    client.join();
    Ok(())
}
```


#### Simple CLI demo

```zsh
#!/bin/bash

MITA_CLI="./target/release/mita"  # the default cargo build directory, or anywhere you put the executable
VIEW_NAME="cli-test"
COMP_NAME="progress-loop"
TOTAL=100
URL= # your mita server address
PASSWORD= # your password

# auth on first run
$MITA_CLI auth --url $URL --password $PASSWORD

# loop
for ((i=0; i<=$TOTAL; i+=5)); do
    echo "Push $i/$TOTAL"
    $MITA_CLI push --view $VIEW_NAME progress_bar "$COMP_NAME" "$i" --total $TOTAL
    sleep 1
done
```

## License

| Module | License                        |
|--------|--------------------------------|
| server | [AGPL](./LICENSE)              |
| python client | [MIT](./client/python/LICENSE) |
| rust client | [MIT](./client/rust/LICENSE) |
