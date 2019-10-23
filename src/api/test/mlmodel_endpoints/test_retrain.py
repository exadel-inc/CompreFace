from http import HTTPStatus

from mock import Mock


from src.api.constants import API_KEY_HEADER


def test__when_retrain_endpoint_is_requested__then_starts_training_and_returns_accepted(client, mocker):
    train_async_mock: Mock = mocker.patch('src.api.controller.train_async')

    res = client.post('/retrain')

    assert res.status_code == HTTPStatus.ACCEPTED, res.json
    train_async_mock.assert_called_with('valid-api-key')
