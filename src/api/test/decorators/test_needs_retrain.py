import pytest
from src.api._decorators import needs_retrain
from src.api.controller import app
from http import HTTPStatus
from src.api.constants import API_KEY_HEADER
from src.api.test.constants import SUCCESS_BODY, VALID_API_KEY

ROUTE = '/test-retrain-endpoint'

@pytest.fixture(scope='module')
def client_with_retrain_endpoint():
    @app.route(ROUTE, methods=['POST'])
    @needs_retrain
    def locked_endpoint():
        return SUCCESS_BODY, HTTPStatus.ACCEPTED
    return app.test_client()


def test__given_retrain_flag_value_true__when_needs_retrain_endpoint_is_requested__then_starts_retraining(
        client_with_retrain_endpoint, mocker):

    retrain_mocker = mocker.patch('src.api._decorators.train_async', return_value=[])
    res = client_with_retrain_endpoint.post(ROUTE+'?retrain=true', headers={API_KEY_HEADER: VALID_API_KEY})
    retrain_mocker.assert_called()
    assert res.status_code == HTTPStatus.ACCEPTED
    assert res.data.decode() == SUCCESS_BODY


def test__given_retrain_flag_value_1__when_needs_retrain_endpoint_is_requested__then_starts_retraining(
        client_with_retrain_endpoint, mocker):
    retrain_mocker = mocker.patch('src.api._decorators.train_async', return_value=[])
    res = client_with_retrain_endpoint.post(ROUTE + '?retrain=1', headers={API_KEY_HEADER: VALID_API_KEY})
    retrain_mocker.assert_called()
    assert res.status_code == HTTPStatus.ACCEPTED
    assert res.data.decode() == SUCCESS_BODY


def test__given_retrain_flag_value_false__when_needs_retrain_endpoint_is_requested__then_skips_retraining(
        client_with_retrain_endpoint, mocker):
    retrain_mocker = mocker.patch('src.api._decorators.train_async', return_value=[])
    res = client_with_retrain_endpoint.post(ROUTE + '?retrain=false', headers={API_KEY_HEADER: VALID_API_KEY})
    retrain_mocker.assert_not_called()
    assert res.status_code == HTTPStatus.ACCEPTED
    assert res.data.decode() == SUCCESS_BODY

def test__given_retrain_flag_value_0__when_needs_retrain_endpoint_is_requested__then_skips_retraining(
        client_with_retrain_endpoint, mocker):
    retrain_mocker = mocker.patch('src.api._decorators.train_async', return_value=[])
    res = client_with_retrain_endpoint.post(ROUTE + '?retrain=0', headers={API_KEY_HEADER: VALID_API_KEY})
    retrain_mocker.assert_not_called()
    assert res.status_code == HTTPStatus.ACCEPTED
    assert res.data.decode() == SUCCESS_BODY


def test__given_retrain_flag_any_other_string__when_needs_retrain_endpoint_is_requested__then_returns_error(
        client_with_retrain_endpoint, mocker):
    message = 'Retrain parameter accepts only true and false'
    retrain_mocker = mocker.patch('src.api._decorators.train_async', return_value=[])
    res = client_with_retrain_endpoint.post(ROUTE+ '?retrain=retraining', headers={API_KEY_HEADER: VALID_API_KEY})
    retrain_mocker.assert_not_called()
    assert res.status_code == HTTPStatus.BAD_REQUEST
    assert res.json['message'] == message


def test__given_no_retrain_flag__when_needs_retrain_endpoint_is_requested__then_starts_retraining(
        client_with_retrain_endpoint, mocker):
    retrain_mocker = mocker.patch('src.api._decorators.train_async', return_value=[])
    res = client_with_retrain_endpoint.post(ROUTE, headers={API_KEY_HEADER: VALID_API_KEY})
    retrain_mocker.assert_called()
    assert res.status_code == HTTPStatus.ACCEPTED
    assert res.data.decode() == SUCCESS_BODY

