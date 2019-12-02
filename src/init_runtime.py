import logging

from dotenv import load_dotenv, find_dotenv

from src.storage.constants import MONGO_HOST, MONGO_PORT


def init_runtime():
    logging.basicConfig(level=logging.DEBUG)
    load_dotenv(find_dotenv(), verbose=True)
    logging.debug(f'Using MongoDB at {MONGO_HOST}:{MONGO_PORT}')
