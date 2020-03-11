import logging

from src.api.controller import init_app

if __name__ == '__main__':
    logging.basicConfig(level=logging.DEBUG)
    debug_app = init_app()
    debug_app.config.from_mapping(SECRET_KEY='dev')
    debug_app.run(host='0.0.0.0', port=3000, debug=True, use_debugger=False, use_reloader=False)
