import pytest
from pymongo import MongoClient
from pymongo.errors import ServerSelectionTimeoutError

from src.constants import ENV
from src.ml_requests import ml_wait_until_ml_is_available


def after_previous_gen():
    order_no = 1
    while True:
        yield order_no
        order_no += 1


after_previous = after_previous_gen()


@pytest.mark.run(order=next(after_previous))
def test_init():
    print(ENV.__str__())
    drop_db_if_needed()
    ml_wait_until_ml_is_available()


def drop_db_if_needed():
    try:
        client = MongoClient(ENV.MONGODB_HOST, ENV.MONGODB_PORT)
        if not ENV.DROP_DB:
            print(f"Skipping database drop: Variable 'DROP_DB' is set to 'false'")
            return
        if 'tmp' not in ENV.MONGODB_DBNAME:
            print(f"Skipping database drop: Database '{ENV.MONGODB_DBNAME}' is not a temporary database")
            return
        print(f"Database drop: Connecting to database")
        if ENV.MONGODB_DBNAME not in client.list_database_names():
            print(f"Skipping database drop: Database '{ENV.MONGODB_DBNAME}' not found")
            return
        client.drop_database(ENV.MONGODB_DBNAME)
    except ServerSelectionTimeoutError:
        pytest.exit(f"Database drop: Failed. Couldn't connect to the database", returncode=1)
    print(f"Database drop: Success")
