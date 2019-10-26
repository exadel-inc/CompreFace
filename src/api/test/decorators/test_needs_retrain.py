from http import HTTPStatus

import pytest

from src.api._decorators import needs_retrain
from src.pyutils.pytest_utils import Expando


@pytest.fixture
def client_with_retrain_endpoint(app):
    @app.route('/endpoint', methods=['POST'])
    @needs_retrain
    def endpoint():
        return 'success-body', HTTPStatus.OK

    return app.test_client()


def test__given_retrain_decorator_raises_error__when_needs_endpoint_is_requested__then_does_not_call_endpoint_function(
        app, mocker):
    # Arrange
    flags = Expando()
    flags.endpoint_was_executed = None

    @app.route('/endpoint', methods=['POST'])
    @needs_retrain
    def endpoint():
        flags.endpoint_was_executed = True
        return 'success-body', HTTPStatus.OK

    mocker.patch('src.api._decorators.train_async')
    client = app.test_client()

    # Act
    res = client.post('/endpoint?retrain=raise-error', headers={'X-Api-Key': 'api-key-001'})

    # Assert
    assert res.status_code == HTTPStatus.BAD_REQUEST, res.json
    assert flags.endpoint_was_executed is None
    assert res.json['message'] == 'Retrain parameter accepts only true and false'


def test__given_endpoint_raises_error__when_needs_retrain_endpoint_is_requested__then_skips_retraining(
        app, mocker):
    # Arrange
    @app.route('/endpoint', methods=['POST'])
    @needs_retrain
    def endpoint():
        raise Exception

    train_async_mock = mocker.patch('src.api._decorators.train_async')
    client = app.test_client()

    # Act
    res = client.post('/endpoint?retrain=true', headers={'X-Api-Key': 'api-key-001'})

    # Assert
    assert res.status_code == HTTPStatus.INTERNAL_SERVER_ERROR, res.json
    train_async_mock.assert_not_called()


def test__when_needs_retrain_endpoint_is_requested__then_starts_retraining_only_after_endpoint_function(
        app, mocker):
    # Arrange
    flags = Expando()
    flags.endpoint_was_executed = None
    flags.endpoint_was_executed_before_training = None

    @app.route('/endpoint', methods=['POST'])
    @needs_retrain
    def endpoint():
        flags.endpoint_was_executed = True
        return 'success-body', HTTPStatus.OK

    def train_async_mock(api_key):
        flags.endpoint_was_executed_before_training = flags.endpoint_was_executed

    mocker.patch('src.api._decorators.train_async', train_async_mock)
    client = app.test_client()

    # Act
    res = client.post('/endpoint?retrain=true', headers={'X-Api-Key': 'api-key-001'})

    # Assert
    assert res.status_code == HTTPStatus.OK, res.json
    assert flags.endpoint_was_executed_before_training is not None
    assert flags.endpoint_was_executed_before_training
    assert res.data.decode() == 'success-body'


def test__given_retrain_flag_value_true__when_needs_retrain_endpoint_is_requested__then_starts_retraining(
        client_with_retrain_endpoint, mocker):
    train_async_mock = mocker.patch('src.api._decorators.train_async')

    res = client_with_retrain_endpoint.post('/endpoint?retrain=true', headers={'X-Api-Key': 'api-key-001'})

    assert res.status_code == HTTPStatus.OK, res.json
    train_async_mock.assert_called()
    assert res.data.decode() == 'success-body'


def test__given_retrain_flag_value_1__when_needs_retrain_endpoint_is_requested__then_starts_retraining(
        client_with_retrain_endpoint, mocker):
    train_async_mock = mocker.patch('src.api._decorators.train_async')

    res = client_with_retrain_endpoint.post('/endpoint?retrain=1', headers={'X-Api-Key': 'api-key-001'})

    assert res.status_code == HTTPStatus.OK, res.json
    train_async_mock.assert_called()
    assert res.data.decode() == 'success-body'


def test__given_retrain_flag_value_false__when_needs_retrain_endpoint_is_requested__then_skips_retraining(
        client_with_retrain_endpoint, mocker):
    train_async_mock = mocker.patch('src.api._decorators.train_async')

    res = client_with_retrain_endpoint.post('/endpoint?retrain=false', headers={'X-Api-Key': 'api-key-001'})

    assert res.status_code == HTTPStatus.OK, res.json
    train_async_mock.assert_not_called()
    assert res.data.decode() == 'success-body'


def test__given_retrain_flag_value_0__when_needs_retrain_endpoint_is_requested__then_skips_retraining(
        client_with_retrain_endpoint, mocker):
    train_async_mock = mocker.patch('src.api._decorators.train_async')

    res = client_with_retrain_endpoint.post('/endpoint?retrain=0', headers={'X-Api-Key': 'api-key-001'})

    assert res.status_code == HTTPStatus.OK, res.json
    train_async_mock.assert_not_called()
    assert res.data.decode() == 'success-body'


def test__given_retrain_flag_any_other_string__when_needs_retrain_endpoint_is_requested__then_returns_error(
        client_with_retrain_endpoint, mocker):
    train_async_mock = mocker.patch('src.api._decorators.train_async')

    res = client_with_retrain_endpoint.post('/endpoint?retrain=raise-error', headers={'X-Api-Key': 'api-key-001'})

    assert res.status_code == HTTPStatus.BAD_REQUEST, res.json
    train_async_mock.assert_not_called()
    assert res.json['message'] == 'Retrain parameter accepts only true and false'


def test__given_no_retrain_flag__when_needs_retrain_endpoint_is_requested__then_starts_retraining(
        client_with_retrain_endpoint, mocker):
    train_async_mock = mocker.patch('src.api._decorators.train_async')

    res = client_with_retrain_endpoint.post('/endpoint', headers={'X-Api-Key': 'api-key-001'})

    assert res.status_code == HTTPStatus.OK, res.json
    train_async_mock.assert_called()
    assert res.data.decode() == 'success-body'
