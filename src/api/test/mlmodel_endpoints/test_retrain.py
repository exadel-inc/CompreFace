from http import HTTPStatus

from mock import Mock


def test__when_get_retrain_endpoint_is_requested__then_starts_training_and_returns_accepted(client, mocker):
    train_async_mock: Mock = mocker.patch('src.api.controller.train_async')

    res = client.post('/retrain')

    assert res.status_code == HTTPStatus.ACCEPTED, res.json
    train_async_mock.assert_called_with('api-key-001')
