import os
from pathlib import Path

from src import init_app

ROOT_DIR = Path(os.path.dirname(os.path.abspath(__file__)))

if __name__ == '__main__':
    app = init_app()
    app.config.from_mapping(SECRET_KEY='dev')
    app.run(debug=True, use_debugger=False, use_reloader=False)
