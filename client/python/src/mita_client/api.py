from __future__ import annotations
from typing import TYPE_CHECKING

import json
import warnings
from enum import Enum
from functools import partial
from queue import Queue
from threading import Thread
from time import sleep
from urllib import request
from urllib.error import HTTPError, URLError

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


_token = ""


def set_token(token: str):
    global _token
    _token = token


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
        req = request.Request(f"{url}/api/auth", data=data, headers={"Content-Type": "application/json"})
        with request.urlopen(req) as f:
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
            "X-Auth-Token": _token
        })
        with request.urlopen(req) as f:
            f.read()
        return State.SUCCESS
    except HTTPError:
        return State.AUTH_ERROR
    except (URLError, TimeoutError):
        return State.CONNECTION_ERROR


class MitaWorker:

    def __init__(self, url: str, client: Mita):
        super().__init__()
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
            import time
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
