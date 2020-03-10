from http import HTTPStatus

import pytest

from src.api.endpoint_decorators import needs_retrain
from src._pyutils.pytest_utils import Expando


@pytest.fixture
def client_with_retrain_endpoint(app):
    @app.route('/endpoint', methods=['POST'])
    @needs_retrain
    def endpoint():
        return 'success-body', HTTPStatus.OK

    return app.test_client()


@pytest.mark.parametrize("retrain_arg, retrain_action",
                         [(None, 'force'), ('yes', 'yes'), ('force', 'force'), ('no', 'no')])
def test__given_retrain_flag_value__when_needs_retrain_endpoint_is_requested__then_starts_or_skips_retraining_depending_on_value(
        client_with_retrain_endpoint, mocker, retrain_arg, retrain_action):
    train_async_mock = mocker.patch('src.api.endpoint_decorators.start_training')

    endpoint = f'/endpoint?retrain={retrain_arg}' if retrain_arg is not None else '/endpoint'
    res = client_with_retrain_endpoint.post(endpoint, headers={'X-Api-Key': 'test-api-key'})

    assert res.status_code == HTTPStatus.OK, res.json
    assert train_async_mock.called == (retrain_action in ('yes', 'force'))
    assert res.data.decode() == 'success-body'


@pytest.mark.parametrize("retrain_arg", ['', 'unknown_value'])
def test__given_retrain_flag_empty_or_unknown_value__when_needs_retrain_endpoint_is_requested__then_returns_error(
        client_with_retrain_endpoint, mocker, retrain_arg):
    train_async_mock = mocker.patch('src.api.endpoint_decorators.start_training')

    res = client_with_retrain_endpoint.post(f'/endpoint?retrain={retrain_arg}', headers={'X-Api-Key': 'test-api-key'})

    assert res.status_code == HTTPStatus.BAD_REQUEST, res.json
    train_async_mock.assert_not_called()
    assert res.json['message'] == "'retrain' parameter accepts only 'YES, NO, FORCE' values"


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

    def train_async_mock(api_key, force=None):
        flags.endpoint_was_executed_before_training = flags.endpoint_was_executed

    mocker.patch('src.api.endpoint_decorators.start_training', train_async_mock)
    client = app.test_client()

    # Act
    res = client.post('/endpoint?retrain=force', headers={'X-Api-Key': 'test-api-key'})

    # Assert
    assert res.status_code == HTTPStatus.OK, res.json
    assert flags.endpoint_was_executed_before_training is not None
    assert flags.endpoint_was_executed_before_training
    assert res.data.decode() == 'success-body'


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

    mocker.patch('src.api.endpoint_decorators.start_training')
    client = app.test_client()

    # Act
    res = client.post('/endpoint?retrain=raise-error', headers={'X-Api-Key': 'test-api-key'})

    # Assert
    assert res.status_code == HTTPStatus.BAD_REQUEST, res.json
    assert flags.endpoint_was_executed is None
    assert res.json['message'] == "'retrain' parameter accepts only 'YES, NO, FORCE' values"


def test__given_endpoint_raises_error__when_needs_retrain_endpoint_is_requested__then_skips_retraining(
        app, mocker):
    # Arrange
    @app.route('/endpoint', methods=['POST'])
    @needs_retrain
    def endpoint():
        raise Exception

    train_async_mock = mocker.patch('src.api.endpoint_decorators.start_training')
    client = app.test_client()

    # Act
    res = client.post('/endpoint?retrain=force', headers={'X-Api-Key': 'test-api-key'})

    # Assert
    assert res.status_code == HTTPStatus.INTERNAL_SERVER_ERROR, res.json
    train_async_mock.assert_not_called()
