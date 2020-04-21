import pytest
from pymongo import MongoClient
from pymongo.errors import ServerSelectionTimeoutError

from e2e_src.constants import ENV_E2E
from e2e_src.ml_requests import ml_get
from e2e_src.ml_requests import ml_wait_until_ml_is_available


def after_previous_gen():
    order_no = 1
    while True:
        yield order_no
        order_no += 1


after_previous = after_previous_gen()


@pytest.mark.run(order=next(after_previous))
def test_init():
    print(ENV_E2E.to_json())
    drop_db_if_needed()
    ml_wait_until_ml_is_available()


@pytest.mark.run(order=next(after_previous))
def test_automated_tests():
    content = ml_get('/force_fail_e2e_tests').content.decode()
    assert content == 'false', content


def drop_db_if_needed():
    try:
        client = MongoClient(ENV_E2E.MONGODB_HOST, ENV_E2E.MONGODB_PORT)
        if not ENV_E2E.DROP_DB:
            print(f"Skipping database drop: Variable 'DROP_DB' is set to 'false'")
            return
        if 'tmp' not in ENV_E2E.MONGODB_DBNAME:
            print(f"Skipping database drop: Database '{ENV_E2E.MONGODB_DBNAME}' is not a temporary database")
            return
        print("Database drop: Connecting to database")
        if ENV_E2E.MONGODB_DBNAME not in client.list_database_names():
            print(f"Skipping database drop: Database '{ENV_E2E.MONGODB_DBNAME}' not found")
            return
        client.drop_database(ENV_E2E.MONGODB_DBNAME)
    except ServerSelectionTimeoutError:
        pytest.exit(f"Database drop: Failed. Couldn't connect to the database", returncode=1)
    print("Database drop: Success")
