from http import HTTPStatus

from src.api.constants import API_KEY_HEADER
from src.api.test.constants import VALID_API_KEY
FILE_BYTES = b''

def test__when_add_face_example_endpoint_is_requested__then_preprocesses_and_saves_the_face(client, mocker):
    filename = 'test-file.jpg'
    request_data = dict(file=(FILE_BYTES, filename))
    mock = mocker.Mock()
    mock.imread.return_value = []
    mock.save_face.return_value = []
    mock.core.crop_face.return_value = []
    mock.calc_embedding.return_value = None
    moker_imread = mocker.patch('src.api.main.imageio.imread', return_value=mock)
    mocker_crop_face = mocker.patch('src.api.main.core.crop_face', return_value=mock)
    mocker_calc_emb = mocker.patch('src.api.main.core.calc_embedding', return_value=mock)
    mocker.patch('src.api.main.get_storage', return_value=mock)
    res = client.post('/faces/New Face', headers={API_KEY_HEADER: VALID_API_KEY}, content_type='multipart/form-data',
                       data=request_data)

    moker_imread.assert_called()
    mocker_crop_face.assert_called()
    mocker_calc_emb.assert_called()
    mock.save_face.assert_called()
    assert res.status_code == HTTPStatus.CREATED


