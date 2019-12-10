from src.api.exceptions import BadRequestException


def test__given_core_raises_bad_request_error_with_custom_msg__then_returns_400_error_with_custom_msg(
        app):
    @app.route('/_raises_bad_request_error')
    def _raises_bad_request_error():
        raise BadRequestException('Detailed bad request information')

    client = app.test_client()

    res = client.get('/_raises_bad_request_error')

    assert res.status_code == 400
    assert res.json['message'] == 'Detailed bad request information'


def test__given_core_raises_bad_request_error_with_no_custom_msg__then_returns_400_error_with_default_message(
        app):
    @app.route('/_raises_bad_request_error')
    def _raises_bad_request_error():
        raise BadRequestException

    client = app.test_client()

    res = client.get('/_raises_bad_request_error')

    assert res.status_code == 400
    assert res.json['message'] == 'Bad request is provided'


def test__when_some_other_error_is_raised_with_custom_msg__then_returns_500_with_custom_msg(app):
    @app.route('/_raises_error')
    def _raises_error():
        raise Exception('Detailed server error information')

    client = app.test_client()

    res = client.get('/_raises_error')

    assert res.status_code == 500
    assert res.json['message'] == 'Detailed server error information'


def test__when_some_other_error_is_raised_with_no_msg__then_returns_500_with_no_message(app):
    @app.route('/_raises_error')
    def _raises_error():
        raise Exception

    client = app.test_client()

    res = client.get('/_raises_error')

    assert res.status_code == 500
    assert res.json['message'] == ''
