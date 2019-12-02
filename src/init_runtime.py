import logging

from dotenv import load_dotenv


def init_runtime():
    logging.basicConfig(level=logging.DEBUG)
    load_dotenv()
