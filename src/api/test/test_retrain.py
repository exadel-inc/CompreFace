from http import HTTPStatus
from unittest.mock import Mock

from src.api.constants import API_KEY_HEADER
from src.api.test.constants import VALID_API_KEY


def test__when_retrain_endpoint_is_requested__then_starts_training_and_returns_accepted(client, mocker):
    mock: Mock = mocker.patch('src.api.app.train_async')

    res = client.post('/retrain', headers={API_KEY_HEADER: VALID_API_KEY})

    mock.assert_called_with(VALID_API_KEY)
    assert res.status_code == HTTPStatus.ACCEPTED
