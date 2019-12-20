import functools
from typing import Iterable


class Expando:
    pass


def one(iterable: Iterable):
    """ Ensures that iterable contains only one element and returns it """

    list_ = list(iterable)
    assert len(list_) == 1
    return list_[0]


def pass_through_decorator(*ignored_args, **ignored_kwargs):
    """ This decorator does nothing. Decorator arguments are ignored. """

    def decorator(func):
        @functools.wraps(func)
        def wrapper_decorator(*args, **kwargs):
            return func(*args, **kwargs)

        return wrapper_decorator

    return decorator
