from http import HTTPStatus


def test__when_status_endpoint_is_requested__then_returns_200(client):
    pass  # Arrange-Act-Assert testing pattern is used. Nothing is to be Arranged for this test, therefore "pass".

    res = client.get('/status')

    assert res.status_code == 200
    assert res.json['status'] == 'OK'
