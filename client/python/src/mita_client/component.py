from abc import ABC
from dataclasses import dataclass, asdict
from typing import Generic, TypeVar, Union, List, Dict

T = TypeVar("T")


@dataclass
class Component(ABC, Generic[T]):
    cls: str
    name: str
    value: T

    def to_dict(self) -> dict:
        return asdict(self)


class Variable(Component[T]):

    def __init__(self, name: str, value: T):
        super().__init__("Variable", name, value)

    def set(self, value: T):
        self.value = value


class ProgressBar(Component[Union[int, float]]):

    def __init__(self, name: str, value: Union[int, float] = 0, total: Union[int, float] = 100):
        super().__init__("ProgressBar", name, value)
        self.total = total

    def to_dict(self) -> dict:
        return {**super().to_dict(), "total": self.total}

    def set(self, value: Union[int, float]):
        self.value = value


class Logger(Component[List[str]]):

    def __init__(self, name: str):
        super().__init__("Logger", name, [])

    def log(self, value: str):
        self.value.append(value)

    def to_dict(self) -> dict:
        return {**super().to_dict()}


class LineChart(Component[List[Dict[str, Union[float, str]]]]):

    def __init__(self, name: str, x_label: str = "index", y_label: str = "value"):
        super().__init__("LineChart", name, [])
        self.x_label = x_label
        self.y_label = y_label

    def add(self, x: float, y: float, label: str = ""):
        self.value.append({"x": x, "y": y, "label": label})

    def to_dict(self) -> dict:
        return {**super().to_dict(), "x_label": self.x_label, "y_label": self.y_label}
