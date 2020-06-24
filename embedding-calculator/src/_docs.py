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

from flasgger import Swagger

from src.docs import DOCS_DIR


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
