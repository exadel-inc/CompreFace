import functools
from typing import Callable, Type


class Expando:
    pass


def raises(e: Type[Exception], callable_: Callable):
    try:
        callable_()
    except e:
        return True
    else:
        return False


def pass_through_decorator(*ignored_args, **ignored_kwargs):
    """ This decorator does nothing. Decorator arguments are ignored. """

    def decorator(func):
        @functools.wraps(func)
        def wrapper_decorator(*args, **kwargs):
            return func(*args, **kwargs)

        return wrapper_decorator

    return decorator
