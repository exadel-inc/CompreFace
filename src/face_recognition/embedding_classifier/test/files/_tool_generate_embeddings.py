import os
from pathlib import Path

import imageio
import joblib

from src.face_recognition.embedding_calculator.calculator import calculate_embedding
from src.face_recognition.face_cropper.cropper import crop_face

CURRENT_DIR = Path(os.path.dirname(os.path.realpath(__file__)))


def generate_embedding_from_img(filename):
    img = imageio.imread(CURRENT_DIR / f'{filename}.jpg')
    img = crop_face(img).img
    embedding = calculate_embedding(img)
    joblib.dump(embedding, f'{filename}.embedding.joblib')


if __name__ == '__main__':
    generate_embedding_from_img('personA-img1')
    generate_embedding_from_img('personA-img2')
    generate_embedding_from_img('personB-img1')
