from flasgger import Swagger

from src.docs import DOCS_DIR
from src.docs2 import DOCS2_DIR


def add_docs(app):
    app.config['SWAGGER'] = {
        "title": "Swagger UI",
        "doc_dir": str(DOCS_DIR),
        "specs": [
            {
                "endpoint": "frs-core-api",
                "route": "/frs-core-api.json",
                "rule_filter": lambda rule: True,  # all in
                "model_filter": lambda tag: True,  # all in
            }
        ],
        "static_url_path": "/apidocs",
        "swagger_ui": True,
        "specs_route": "/apidocs",
        "endpoint": 'flasgger'
    }
    Swagger(app, template_file=str(DOCS_DIR / 'template.yml'))

    app.config['SWAGGER'] = {
        "title": "Swagger UI",
        "doc_dir": str(DOCS2_DIR),
        "specs": [
            {
                "endpoint": "frs-core-api2",
                "route": "/frs-core-api2.json",
                "rule_filter": lambda rule: True,  # all in
                "model_filter": lambda tag: True,  # all in
            }
        ],
        "static_url_path": "/apidocs2",
        "swagger_ui": True,
        "specs_route": "/apidocs2",
        "endpoint": 'flasgger2'
    }
    Swagger(app, template_file=str(DOCS2_DIR / 'template.yml'))
