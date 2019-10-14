def test__when_status_is_requested__then_returns_ok_response(app):
    pass

    res = app.get('/status')

    assert res.status_code == 200
    assert res.json['status'] == 'OK'


def test__when_no_api_key_is_given__then_returns_unauthorized_response(app):
    pass

    res = app.get('/faces')

    assert res.status_code == 401
    assert res.json['message'] == "No API Key is given"
