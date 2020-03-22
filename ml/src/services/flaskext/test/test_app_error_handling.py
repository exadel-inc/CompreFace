from werkzeug.exceptions import NotFound


def test__given_raises_value_error__when_called__then_returns_500(app):
    @app.route('/endpoint')
    def endpoint():
        raise ValueError

    res = app.test_client().get('/endpoint')

    assert res.status_code == 500
    assert res.json['message'] == 'ValueError'


def test__given_raises_value_error_with_msg__when_called__then_returns_500_with_msg(app):
    @app.route('/endpoint')
    def endpoint():
        raise ValueError('Detailed server error information')

    res = app.test_client().get('/endpoint')

    assert res.status_code == 500
    assert res.json['message'] == 'ValueError: Detailed server error information'


def test__given_raises_not_found_error__when_called__then_returns_404(app):
    pass

    res = app.test_client().get('/endpoint')

    assert res.status_code == 404
    assert res.json['message'] == '404 Not Found: The requested URL was not found on the server. ' \
                                  'If you entered the URL manually please check your spelling ' \
                                  'and try again.'


def test__given_raises_not_found_error_with_msg__when_called__then_returns_404_with_msg(app):
    @app.route('/endpoint')
    def endpoint():
        raise NotFound('Detailed error information')

    res = app.test_client().get('/endpoint')

    assert res.status_code == 404
    assert res.json['message'] == '404 Not Found: Detailed error information'
