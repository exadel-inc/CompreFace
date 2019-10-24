import os
from pathlib import Path
import pytest
import imageio
from src.face_recognition.face_cropper.cropper import crop_faces
from src.face_recognition.embedding_calculator.calculator import calculate_embedding
from src.storage.storage_factory import get_storage
from src.face_recognition.embedding_classifier.classifier import get_face_predictions, train_async

CURRENT_DIR = Path(os.path.dirname(os.path.realpath(__file__)))

def add_image(path, name, api_key):
    im = imageio.imread(path)
    cropped_faces = crop_faces(im)[0].img
    embedding = calculate_embedding(cropped_faces)
    get_storage().add_face(raw_img=im, face_img=cropped_faces, embedding=embedding, face_name=name,
                           api_key=api_key)
    train_async(api_key)

@pytest.mark.integration
def test_added_two_faces_to_the_database_recognizes_the_third_person():

    api_key = 1111
    add_image(CURRENT_DIR / 'files' / 'personA-img1.jpg', 'Person A', api_key)
    add_image(CURRENT_DIR / 'files' / 'personB-img1.jpg', 'Person B', api_key)
    img = imageio.imread(CURRENT_DIR / 'files' / 'personA-img1.jpg')

    face_predictions = get_face_predictions(img, 1, api_key)

    assert face_predictions[0].prediction == 'Person A'
