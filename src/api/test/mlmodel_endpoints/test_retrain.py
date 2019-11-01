from http import HTTPStatus

from mock import Mock


def test__when_post_retrain_endpoint_is_requested__then_starts_training_and_returns_accepted(client, mocker):
    train_async_mock: Mock = mocker.patch('src.api.controller.train_async')

    res = client.post('/retrain')

    assert res.status_code == HTTPStatus.ACCEPTED, res.json
    train_async_mock.assert_called_with('api-key-001')

def test__when_get_retrain_endpoint_is_requested__then_if_model_is_not_retraining_then_start_retraining__return_200(client, mocker):
    train_async_mock: Mock = mocker.patch('src.api.controller.is_currently_training', return_value = False)

    res = client.get('/retrain')

    assert res.status_code == HTTPStatus.OK, res.json
    train_async_mock.assert_called_with('api-key-001')


def test__when_get_retrain_endpoint_is_requested__then_if_model_is_already_retraining_then_no_retraining_return_202(client,
                                                                                                              mocker):
    train_async_mock: Mock = mocker.patch('src.api.controller.is_currently_training', return_value=True)

    res = client.get('/retrain')

    assert res.status_code == HTTPStatus.ACCEPTED, res.json
    train_async_mock.assert_called_with('api-key-001')


def test__when_delete_retrain_endpoint_is_requested__then_retraining_is_ensured_to_be_stopped(client, mocker):
    train_async_mock: Mock = mocker.patch('src.api.controller.cancel_training')

    res = client.delete('/retrain')

    assert res.status_code == HTTPStatus.NO_CONTENT, res.json
    train_async_mock.assert_called_with('api-key-001')

