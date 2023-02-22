from __future__ import annotations

from typing import List
from urllib.error import URLError

from .api import auth, MitaWorker, State
from .error import MitaAuthError
from .view import View


class Mita:

    def __init__(self, url: str, password: str, num_workers: int = 1, verbose: bool = False):
        self.url = url
        self.password = password
        self.views: List[View] = []
        self.worker = MitaWorker(url, self)
        self.num_workers = num_workers
        self.verbose = verbose

    def __enter__(self) -> Mita:
        self.auth()
        for _ in range(self.num_workers):
            self.worker.start_thread()
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.worker.stop()

    def auth(self) -> None:
        state = auth(self.url, self.password)
        if state == State.SUCCESS:
            return
        elif state == State.AUTH_ERROR:
            raise MitaAuthError("Authentication failed.")
        elif state == State.CONNECTION_ERROR:
            raise URLError("Connection failed.")

    def add(self, *view: View) -> Mita:
        self.views.extend(view)
        return self

    def push(self) -> None:
        for view in self.views:
            self.worker.put(view.to_dict())
