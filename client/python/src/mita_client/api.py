from __future__ import annotations
from typing import TYPE_CHECKING

import json
import time
import warnings
from enum import Enum
from functools import partial
from queue import Queue
from threading import Thread, local
from time import sleep
from urllib import request
from urllib.error import HTTPError, URLError

from ._version import version
from .error import MitaAuthError

if TYPE_CHECKING:
    from .client import Mita


class State(Enum):
    """
    State of response.
    """
    SUCCESS = 0
    AUTH_ERROR = 1
    CONNECTION_ERROR = 2


# Use thread-local storage for token to ensure thread safety
_thread_local = local()


def set_token(token: str):
    _thread_local.token = token


def get_token():
    return getattr(_thread_local, 'token', "")


# POST /api/auth
def auth(url: str, password: str) -> State:
    """
    Request authentication to the server.

    Args:
        url (``str``): URL of the server.
        password (``str``): Password of the server.

    Returns:
        ``State``: State of response.
    """
    try:
        data = str(json.dumps({"password": password})).encode("utf-8")
        req = request.Request(f"{url}/api/auth", data=data, headers={
            "Content-Type": "application/json",
            "User-Agent": f"mita_client_python/{version}"
        })
        with request.urlopen(req, timeout=30) as f:
            set_token(json.loads(f.read())["token"])
        return State.SUCCESS
    except HTTPError:
        return State.AUTH_ERROR
    except (URLError, TimeoutError):
        return State.CONNECTION_ERROR


# POST /api/push
def push(url: str, data: dict) -> State:
    """
    Push data to the server.

    Args:
        url (``str``): URL of the server.
        data (``dict``): Data to push.

    Returns:
        ``State``: State of response.
    """
    try:
        data = str(json.dumps(data)).encode("utf-8")
        req = request.Request(f"{url}/api/push", data=data, headers={
            "Content-Type": "application/json",
            "X-Auth-Token": get_token()
        })
        with request.urlopen(req, timeout=30) as f:
            f.read()
        return State.SUCCESS
    except HTTPError:
        return State.AUTH_ERROR
    except (URLError, TimeoutError):
        return State.CONNECTION_ERROR


class MitaWorker:

    def __init__(self, url: str, client: Mita):
        self.client = client
        self.push = partial(push, url)
        self.data = Queue()  # queue of dict
        self.should_stop = False
        self.threads = []

    def put(self, data: dict):
        """Put data to the queue."""
        self.data.put(data)

    def stop(self):
        """Stop the threads."""
        self.should_stop = True

    def start_thread(self):
        """Start a thread to push data."""
        t = Thread(target=self._thread_job)
        t.start()
        self.threads.append(t)

    def join(self):
        """Join all threads."""
        for t in self.threads:
            t.join()

    def _thread_job(self):
        while True:
            if self.data.qsize() == 0:
                if self.should_stop:
                    break
                else:
                    sleep(0.1)
                    continue

            d = self.data.get()
            t0 = time.time()
            state = self.push(d)
            if state == State.CONNECTION_ERROR:
                warnings.warn(f"[Mita] Connection error!")
            elif state == State.AUTH_ERROR:
                try:
                    self.client.auth()
                except MitaAuthError:
                    warnings.warn(f"[Mita] Authentication error!")
                except URLError:
                    warnings.warn(f"[Mita] Connection error!")
                else:
                    self.push(d)

            if self.client.verbose:
                print(f"[Mita] Pushed in {time.time() - t0:.3f} sec.")
