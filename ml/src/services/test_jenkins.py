from src.constants import ENV


def test_jenkins():
    assert not ENV.FORCE_FAIL_UNIT_TESTS
