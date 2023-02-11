from __future__ import annotations

from typing import List

from .component import Component


class View:

    def __init__(self, name: str):
        self.data: List[Component] = []
        self.name = name

    def add(self, *component: Component) -> View:
        self.data.extend(component)
        return self

    def to_dict(self) -> dict:
        return {
            "view": self.name,
            "data": [c.to_dict() for c in self.data]
        }
