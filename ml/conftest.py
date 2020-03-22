import os
import sys
from pathlib import Path

sys.path.insert(0, os.path.dirname(__file__))


def pytest_ignore_collect(path):
    return 'extlib' in Path(path).parts
