import logging

import imageio
import joblib

from src.cache import get_scanner
from src.services.facescan.backend.facescan_backend import FacescanBackend
from src.services.sample_images import IMG_DIR
from src.services.utils.pyutils import get_dir

CURRENT_DIR = get_dir(__file__)


def generate_embedding_from_img(filename):
    img = imageio.imread(IMG_DIR / f'{filename}.jpg')
    scanner: FacescanBackend = get_scanner()
    embedding = scanner.scan_one(img).embedding
    joblib.dump(embedding, CURRENT_DIR / f'{filename}.embedding.joblib')


if __name__ == '__main__':
    logging.basicConfig(level=logging.DEBUG)
    generate_embedding_from_img('personA-img1')
    generate_embedding_from_img('personA-img2')
    generate_embedding_from_img('personB-img1')
