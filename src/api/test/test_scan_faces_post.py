from http import HTTPStatus

FILE_BYTES = b''


def test__when_image_uploaded_to_scan_face_endpoint__then_returns_scan_results(mocker, client):
    filename = 'test-file.jpg'
    request_data = dict(file=(FILE_BYTES, filename), limit=0)
    expected_names = [{"embedding": [34, 31, 53, 64]},
                      {"embedding": [100, 32, 50, 22]}]

    mocker.patch('src.api.controller.imageio.imread', return_value=[])
    mocker.patch('src.api.controller.scan_faces', return_value=[{"embedding": [34, 31, 53, 64]},
                                                                {"embedding": [100, 32, 50, 22]}])
    res = (client.post('/scan_faces', content_type='multipart/form-data',
                       data=request_data))

    assert res.status_code == HTTPStatus.OK
    assert res.json['result'] == expected_names
