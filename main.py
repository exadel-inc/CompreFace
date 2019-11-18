import os
from pathlib import Path

from src.init_runtime import init_runtime

ROOT_DIR = Path(os.path.dirname(os.path.realpath(__file__)))


def init_app():  # Hides the dependency for scripts which only take the ROOT_DIR variable from this file
    from src.api.controller import init_app
    return init_app()


if __name__ == '__main__':
    init_runtime()
    debug_app = init_app()
    debug_app.config.from_mapping(SECRET_KEY='dev')
    debug_app.run(host='0.0.0.0', debug=True, use_debugger=False, use_reloader=False)
