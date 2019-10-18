import os

import imageio

from src.faceembedder import calc_embedding

SCRIPT_DIR = os.path.dirname(os.path.realpath(__file__))


def test__when_given_two_faces_of_the_same_person__then_returns_equal_embeddings():
    person_a_im1 = imageio.imread(f'{SCRIPT_DIR}/files/personA-img1-cropped.jpg')
    person_a_im2 = imageio.imread(f'{SCRIPT_DIR}/files/personA-img2-cropped.jpg')

    person_a_face_embedding1 = calc_embedding(person_a_im1)
    person_a_face_embedding2 = calc_embedding(person_a_im2)

    assert person_a_face_embedding1 == person_a_face_embedding2


def test__when_given_two_faces_of_different_people__then_returns_different_embeddings():
    person_a_im = imageio.imread(f'{SCRIPT_DIR}/files/personA-img1-cropped.jpg')
    person_b_im = imageio.imread(f'{SCRIPT_DIR}/files/personB-img1-cropped.jpg')

    person_a_face_embedding = calc_embedding(person_a_im)
    person_b_face_embedding = calc_embedding(person_b_im)

    assert person_a_face_embedding == person_b_face_embedding
