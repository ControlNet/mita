from typing import Optional, Union

from tqdm.auto import tqdm as std_tqdm

from .component import ProgressBar
from .client import Mita
from .view import View


class MitaTqdm(std_tqdm):

    """
    A wrapper class for tqdm that sends progress to Mita.

    Args:
        iterable: Iterable to decorate with a progressbar.
        address (``str``): The address of the Mita server.
        password (``str``): The password of the Mita server.
        view (``str`` | ``View``, optional): The view used for mita client. If the input is `str`, it will be used as
            the view name. Default name is the hostname.
        *args: Arguments passed to the wrapped `tqdm` instance.
        **kwargs: Keyword arguments passed to the wrapped `tqdm` instance.

    Please note that the `desc` argument of the wrapped `tqdm` instance is used as the name of the progress bar.

    Examples::

        >>> from mita_client import mita_tqdm
        >>> import time
        >>> for i in mita_tqdm(range(1000), ADDRESS, PASSWORD):
        ...     time.sleep(0.1)

    """

    def __init__(self, iterable=None,
        address: Optional[str] = None,
        password: Optional[str] = None,
        view: Optional[Union[str, View]] = None, *args, **kwargs
    ):
        super().__init__(iterable, *args, **kwargs)
        assert address is not None and password is not None, "address and password must be specified."
        if view is None:
            view = View()
        elif view is not None and isinstance(view, str):
            view = View(view)
        elif view is not None and not isinstance(view, View):
            raise TypeError("view must be either None, str or View.")

        if self.desc == "":
            self.desc = "Progress"

        self.progress_bar = ProgressBar(self.desc, total=self.total or 1)
        view.add(self.progress_bar)
        self.client = Mita(address, password, verbose=True)
        self.client.add(view)
        self.client.__enter__()

    def update(self, n=1):
        displayed = super().update(n)
        self.progress_bar.value = self.n
        if self.total is not None:
            self.progress_bar.total = self.total

        if displayed:
            self.client.push()
        return displayed

    def close(self):
        self.client.push()
        self.client.__exit__(None, None, None)
        super().close()
