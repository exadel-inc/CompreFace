import functools
import os
from io import BytesIO
from pathlib import Path

import joblib


def run_once(func):
    """ Runs the function only once (caches the return value for subsequent runs) """

    @functools.wraps(func)
    def decorator(*args, **kwargs):
        if decorator.has_run:
            return decorator.result
        decorator.has_run = True
        decorator.result = func(*args, **kwargs)
        return decorator.result

    decorator.has_run = False
    decorator.result = None
    return decorator


def run_first(preceding_func):
    """ Runs some function before running decorated function """

    def wrapper_decorator(func):
        @functools.wraps(func)
        def decorator(*args, **kwargs):
            preceding_func()
            returned_value = func(*args, **kwargs)
            return returned_value

        return decorator

    return wrapper_decorator


def cached(func):
    """ Caches the return values for a function with one argument """
    cache = {}

    @functools.wraps(func)
    def decorator(arg):
        if arg in cache:
            return cache[arg]

        result = func(arg)
        cache[arg] = result
        return result

    return decorator


def first_and_only(lst):
    length_lst = len(lst)
    assert length_lst == 1, f"Item count is1 '{length_lst}' instead of '1'"
    return lst[0]


def get_dir(filepath):
    return Path(os.path.dirname(os.path.realpath(filepath)))


def serialize(obj: object) -> bytes:
    bytes_container = BytesIO()
    joblib.dump(obj, bytes_container)  # Works better with numpy arrays than pickle
    bytes_container.seek(0)  # update to enable reading
    bytes_data = bytes_container.read()
    return bytes_data


def deserialize(bytes_data: bytes) -> object:
    bytes_container = BytesIO(bytes_data)
    obj = joblib.load(bytes_container)
    return obj
