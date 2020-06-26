#  Copyright (c) 2020 the original author or authors
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       https://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
#  or implied. See the License for the specific language governing
#  permissions and limitations under the License.

import functools
import json
import os
import re
from pathlib import Path
from typing import Tuple, List

import numpy
from scipy.spatial import distance


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


def get_current_dir(__file__):
    return Path(os.path.dirname(os.path.realpath(__file__)))


def get_env(name: str, default: str = None) -> str:
    if default is None:
        return os.environ[name]
    return os.environ.get(name, '') or default


def get_env_bool(name: str, default: bool = False) -> bool:
    return Constants.str_to_bool(get_env(name, str(default)))


def get_env_split(name: str, default: str = None):
    return Constants.split(get_env(name, default))


class Constants:
    @classmethod
    def _get_constants(cls):
        names = (name for name in dir(cls)
                 if not name.startswith('_')
                 and type(getattr(cls, name)).__name__ in ('float', 'int', 'str', 'bool', 'list', 'tuple'))
        return {key: getattr(cls, key) for key in names}

    @classmethod
    def to_str(cls):
        return str(cls._get_constants())

    @classmethod
    def to_json(cls):
        return json.dumps(cls._get_constants(), indent=4)

    @staticmethod
    def str_to_bool(string: str):
        return string.lower() in ('true', '1')

    @staticmethod
    def split(arr_str):
        """
        >>> Constants.split("One")
        ['One']
        >>> Constants.split("One Two")
        ['One', 'Two']
        >>> Constants.split("One  , Two")
        ['One', 'Two']
        >>> Constants.split("One,Two")
        ['One', 'Two']
        >>> Constants.split("One, Two")
        ['One', 'Two']
        >>> Constants.split(" One Two ")
        ['One', 'Two']
        """
        return [s for s in re.split(r'[,\s]+', arr_str) if s]


def s(count):  # NOSONAR
    return '' if count == 1 else 's'


def get_nearest_point_idx(target_point: Tuple[int, int], points: List[Tuple[int, int]]):
    # noinspection PyTypeChecker
    return distance.cdist([target_point], points).argmin()
