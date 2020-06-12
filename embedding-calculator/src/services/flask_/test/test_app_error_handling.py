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

from werkzeug.exceptions import NotFound

ENDPOINT = '/endpoint'


def test__given_raises_value_error__when_called__then_returns_500(app):
    @app.route(ENDPOINT)
    def endpoint():
        raise ValueError

    res = app.test_client().get(ENDPOINT)

    assert res.status_code == 500
    assert res.json['message'] == 'ValueError'


def test__given_raises_value_error_with_msg__when_called__then_returns_500_with_msg(app):
    @app.route(ENDPOINT)
    def endpoint():
        raise ValueError('Detailed server error information')

    res = app.test_client().get(ENDPOINT)

    assert res.status_code == 500
    assert res.json['message'] == 'ValueError: Detailed server error information'


def test__given_raises_not_found_error__when_called__then_returns_404(app):
    pass  # NOSONAR

    res = app.test_client().get(ENDPOINT)

    assert res.status_code == 404
    assert res.json['message'] == '404 Not Found: The requested URL was not found on the server. ' \
                                  'If you entered the URL manually please check your spelling ' \
                                  'and try again.'


def test__given_raises_not_found_error_with_msg__when_called__then_returns_404_with_msg(app):
    @app.route(ENDPOINT)
    def endpoint():
        raise NotFound('Detailed error information')

    res = app.test_client().get(ENDPOINT)

    assert res.status_code == 404
    assert res.json['message'] == '404 Not Found: Detailed error information'
