import json
import warnings
from enum import Enum
from functools import partial
from queue import Queue
from threading import Thread
from time import sleep
from urllib import request
from urllib.error import HTTPError, URLError


class State(Enum):
    """
    State of response.
    """
    SUCCESS = 0
    AUTH_ERROR = 1
    CONNECTION_ERROR = 2


# GET /api/auth?pw=password
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
        with request.urlopen(f"{url}/api/auth?pw={password}") as f:
            f.read()
        return State.SUCCESS
    except HTTPError:
        return State.AUTH_ERROR
    except URLError:
        return State.CONNECTION_ERROR


# POST /api/push
def push(url: str, password: str, data: dict) -> State:
    """
    Push data to the server.

    Args:
        url (``str``): URL of the server.
        password (``str``): Password of the server.
        data (``dict``): Data to push.

    """
    try:
        data["password"] = password
        data = str(json.dumps(data)).encode("utf-8")
        req = request.Request(f"{url}/api/push", data=data, headers={"Content-Type": "application/json"})
        with request.urlopen(req) as f:
            f.read()
        return State.SUCCESS
    except HTTPError:
        return State.AUTH_ERROR
    except URLError:
        return State.CONNECTION_ERROR


class MitaWorker:

    def __init__(self, url: str, password: str):
        super().__init__()
        self.push = partial(push, url, password)
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
            state = self.push(d)
            print("Pushed.")
            if state == State.CONNECTION_ERROR:
                warnings.warn(f"[Mika] Connection error!")
            elif state == State.AUTH_ERROR:
                warnings.warn(f"[Mika] Authentication error!")

        print("Thread stopped.")