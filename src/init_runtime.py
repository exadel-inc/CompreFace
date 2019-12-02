import logging

from dotenv import load_dotenv, find_dotenv


def init_runtime():
    logging.basicConfig(level=logging.DEBUG)
    load_dotenv(find_dotenv(), verbose=True)
