import pytest

from e2e_src.ml_requests import ml_get
from e2e_src.test.init_test import after_previous


@pytest.mark.run(order=next(after_previous))
def test__when_checking_status__then_returns_200():
    pass

    res = ml_get("/status")

    assert res.status_code == 200, res.content
    assert res.json()['status'] == 'OK'


@pytest.mark.run(order=next(after_previous))
def test__when_opening_apidocs__then_returns_200():
    pass

    res = ml_get("/apidocs")

    assert res.status_code == 200, res.status_code


@pytest.mark.run(order=next(after_previous))
def test__when_opening_apidocs__then_returns_200():
    pass

    res = ml_get("/apidocs2")

    assert res.status_code == 200, res.status_code
