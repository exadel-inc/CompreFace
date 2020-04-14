from src.constants import ENV


def test_automated_tests():
    assert not ENV.FORCE_FAIL_UNIT_TESTS
