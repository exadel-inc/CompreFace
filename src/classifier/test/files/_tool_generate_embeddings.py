import logging
import os
from pathlib import Path

import imageio
import joblib

from src.facescanner._embedder.embedder import calculate_embedding
from src.facescanner._embedder.face_crop import crop_image

CURRENT_DIR = Path(os.path.dirname(os.path.realpath(__file__)))
IMAGES_DIR = CURRENT_DIR


def generate_embedding_from_img(filename):
    img = imageio.imread(IMAGES_DIR / f'{filename}.jpg')
    img = crop_image(img)
    embedding_array = calculate_embedding(img).array
    joblib.dump(embedding_array, CURRENT_DIR / f'{filename}.embedding.joblib')


if __name__ == '__main__':
    logging.basicConfig(level=logging.DEBUG)
    generate_embedding_from_img('personA-img1')
    generate_embedding_from_img('personA-img2')
    generate_embedding_from_img('personB-img1')
