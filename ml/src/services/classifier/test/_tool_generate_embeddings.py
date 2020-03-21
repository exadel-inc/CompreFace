import logging

import imageio
import joblib

from src.runtime import get_scanner
from src.services.utils.pyutils import get_dir
from test.sample_images import IMG_DIR

CURRENT_DIR = get_dir(__file__)


def generate_embedding_from_img(filename):
    img = imageio.imread(IMG_DIR / f'{filename}.jpg')
    embedding = get_scanner().scan_one(img).embedding
    joblib.dump(embedding, CURRENT_DIR / f'{filename}.embedding.joblib')


if __name__ == '__main__':
    logging.basicConfig(level=logging.DEBUG)
    generate_embedding_from_img('personA-img1')
    generate_embedding_from_img('personA-img2')
    generate_embedding_from_img('personB-img1')
