from src.api.controller import init_app

if __name__ == '__main__':
    debug_app = init_app()
    debug_app.config.from_mapping(SECRET_KEY='dev')
    debug_app.run(debug=True, use_debugger=False, use_reloader=True)
