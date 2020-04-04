import functools
import os
from pathlib import Path

import numpy


def run_once(func):
    """ Runs the function only once (caches the return value for subsequent runs) """

    @functools.wraps(func)
    def decorator(*args, **kwargs):
        if decorator.has_run:
            return decorator.result
        decorator.result = func(*args, **kwargs)
        decorator.has_run = True
        return decorator.result

    decorator.has_run = False
    decorator.result = None
    return decorator


def run_once_fork_safe(func):
    """ Runs the function only once (caches the return value for subsequent runs, until the process is forked) """

    @functools.wraps(func)
    def decorator(*args, **kwargs):
        pid = os.getpid()
        if decorator.has_run and pid == decorator.pid:
            return decorator.result
        decorator.has_run = True
        decorator.result = func(*args, **kwargs)
        decorator.pid = pid
        return decorator.result

    decorator.has_run = False
    decorator.result = None
    decorator.pid = os.getpid()
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
    lst = tuple(lst)
    length_lst = len(lst)
    assert length_lst == 1, f"Item count is '{length_lst}' instead of '1'"
    return lst[0]


def equals(a, b):
    if isinstance(a, numpy.ndarray) and isinstance(b, numpy.ndarray):
        return (a == b).all()
    return a == b


def first_like_all(lst):
    lst = tuple(lst)
    first = lst[0]
    for k in lst:
        assert equals(first, k)
    return first


def get_dir(filepath):
    return Path(os.path.dirname(os.path.realpath(filepath)))


class FilteredStream(object):
    def __init__(self, strings_to_filter, stream):
        self.stream = stream
        self.strings_to_filter = strings_to_filter

    def __getattr__(self, attr_name):
        return getattr(self.stream, attr_name)

    def write(self, data):
        for string in self.strings_to_filter:
            if string in data:
                return
        self.stream.write('XXX')
        self.stream.write(data)
        self.stream.flush()

    def flush(self):
        self.stream.flush()
