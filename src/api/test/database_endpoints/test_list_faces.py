from http import HTTPStatus

import pytest


def return_value_for_mock(mocker, val):
    mock = mocker.Mock()
    mock.get_all_face_names.return_value = val
    return mock


@pytest.mark.parametrize("test_input, expected_names",
                         [([], []), (['Joe Bloggs', 'Fred Bloggs'], ['Joe Bloggs', 'Fred Bloggs'])])
def test__given_certain_amount_of_saved_faces__when_list_faces_is_requested__then_returns_array_with_all_names(client,
                                                                                                               mocker,
                                                                                                               test_input,
                                                                                                               expected_names):
    mocker.patch('src.api.controller.get_storage', return_value=return_value_for_mock(mocker, test_input))

    res = client.get('/faces')

    assert res.status_code == HTTPStatus.OK, res.json
    assert res.json['names'] == expected_names
