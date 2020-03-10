from http import HTTPStatus

from src.scan_faces.dto.face import ScannedFace, BoundingBox

FILE_BYTES = b''


def test__when_image_uploaded_to_scan_face_endpoint__then_returns_scan_results(mocker, client):
    filename = 'test-file.jpg'
    request_data = dict(file=(FILE_BYTES, filename), limit=0)
    expected_values = [{'box': {'probability': 0.9999998807907104,
                                'x_max': 261, 'x_min': 48, 'y_max': 314, 'y_min': 39},
                        'embedding': [34, 31, 53, 64]},
                       {'box': {'probability': 0.983591651950151, 'x_max': 56, 'x_min': 200, 'y_max': 311,
                                'y_min': 899},
                        'embedding': [100, 32, 50, 22]}]

    mocker.patch('src.api.controller.imageio.imread', return_value=[])
    mocker.patch('src.api.controller.scan_faces',
                 return_value=[ScannedFace(embedding=[34, 31, 53, 64], box=BoundingBox(probability=0.9999998807907104,
                                                                                       x_max=261, x_min=48, y_max=314,
                                                                                       y_min=39)),
                               ScannedFace(embedding=[100, 32, 50, 22], box=BoundingBox(probability=0.9835916519501510,
                                                                                        x_max=56, x_min=200, y_max=311,
                                                                                        y_min=899))])
    res = (client.post('/scan_faces', content_type='multipart/form-data',
                       data=request_data))

    assert res.status_code == HTTPStatus.OK
    assert res.json['result'] == expected_values
