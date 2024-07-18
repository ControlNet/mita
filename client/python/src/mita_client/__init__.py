from .client import Mita
from .component import Variable, ProgressBar, Logger, LineChart
from .view import View
from ._version import version
__version__ = version


__all__ = [
    "Mita",
    "Variable",
    "ProgressBar",
    "Logger",
    "LineChart",
    "View",
    "__version__"
]

try:
    import tqdm
except ImportError:
    pass
else:
    from .mita_tqdm import MitaTqdm as mita_tqdm
    __all__.append("mita_tqdm")