# TODO EGP-689

import pytest


@pytest.fixture(scope='module')
def client_with_retrain_endpoint():
    return ...


def test__given_no_retrain_flag__when_needs_retrain_endpoint_is_requested__then_starts_retraining(
        client_with_retrain_endpoint):
    ...


def test__given_empty_retrain_flag_val__when_needs_retrain_endpoint_is_requested__then_starts_retraining(
        client_with_retrain_endpoint):
    ...


def test__given_retrain_flag_value_true__when_needs_retrain_endpoint_is_requested__then_starts_retraining(
        client_with_retrain_endpoint):
    ...


def test__given_retrain_flag_value_1__when_needs_retrain_endpoint_is_requested__then_starts_retraining(
        client_with_retrain_endpoint):
    ...


def test__given_retrain_flag_value_false__when_needs_retrain_endpoint_is_requested__then_skips_retraining(
        client_with_retrain_endpoint):
    ...


def test__given_retrain_flag_value_0__when_needs_retrain_endpoint_is_requested__then_skips_retraining(
        client_with_retrain_endpoint):
    ...


def test__given_retrain_flag_value_other__when_needs_retrain_endpoint_is_requested__then_skips_retraining(
        client_with_retrain_endpoint):
    ...
