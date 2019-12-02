import os
from pathlib import Path

import imageio
import joblib

from main import ROOT_DIR
from src.face_recognition.calc_embedding.calculator import calculate_embedding
from src.face_recognition.crop_faces.crop_faces import crop_one_face
from src.init_runtime import init_runtime

CURRENT_DIR = Path(os.path.dirname(os.path.realpath(__file__)))
IMAGES_DIR = ROOT_DIR / 'test_files'


def generate_embedding_from_img(filename):
    img = imageio.imread(IMAGES_DIR / f'{filename}.jpg')
    img = crop_one_face(img).img
    embedding_array = calculate_embedding(img).array
    joblib.dump(embedding_array, CURRENT_DIR / f'{filename}.embedding.joblib')


if __name__ == '__main__':
    init_runtime()
    generate_embedding_from_img('personA-img1')
    generate_embedding_from_img('personA-img2')
    generate_embedding_from_img('personB-img1')
