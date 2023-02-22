from __future__ import annotations

import socket
from typing import List, Optional

from .component import Component


class View:

    def __init__(self, name: Optional[str] = None):
        self.data: List[Component] = []
        self.name = name or socket.gethostname()

    def add(self, *component: Component) -> View:
        self.data.extend(component)
        return self

    def to_dict(self) -> dict:
        return {
            "view": self.name,
            "data": [c.to_dict() for c in self.data]
        }
