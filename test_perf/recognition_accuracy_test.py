import argparse

import imageio
from sklearn.datasets import fetch_lfw_people

from main import ROOT_DIR
from src.init_runtime import init_runtime
from test_perf._data_wrangling import split_train_test, parse_lfw_data
from test_perf._model_wrappers import EfrsLocal, ModelWrapperBase, EfrsRestApi
from test_perf.dto import Dataset

IMG_DIR = ROOT_DIR / 'test_files'


def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('--host')
    parser.add_argument('--test', action='store_true')
    return parser.parse_args()


def get_lfw_dataset() -> Dataset:
    lfw_data = fetch_lfw_people(min_faces_per_person=2, color=True, funneled=False)
    dataset_full = parse_lfw_data(lfw_data)
    return split_train_test(dataset_full)


def get_test_dataset() -> Dataset:
    dataset_full = [
        ('Person A', imageio.imread(IMG_DIR / 'personA-img1.jpg')),
        ('Person A', imageio.imread(IMG_DIR / 'personA-img2.jpg')),
        ('Person B', imageio.imread(IMG_DIR / 'personB-img1.jpg')),
        ('Person D', imageio.imread(IMG_DIR / 'personD-img1.jpg')),
        ('Person D', imageio.imread(IMG_DIR / 'personD-img2.jpg')),
        ('Person E', imageio.imread(IMG_DIR / 'personE-img1.jpg')),
        ('Person E', imageio.imread(IMG_DIR / 'personE-img2.jpg')),
        ('Person E', imageio.imread(IMG_DIR / 'personE-img3.jpg')),
        ('Person F', imageio.imread(IMG_DIR / 'personF-img1.jpg')),
        ('Person F', imageio.imread(IMG_DIR / 'personF-img2.jpg')),
        ('Person G', imageio.imread(IMG_DIR / 'personG-img1.jpg')),
        ('Person G', imageio.imread(IMG_DIR / 'personG-img2.jpg')),
        ('Person G', imageio.imread(IMG_DIR / 'personG-img3.jpg')),
        ('Person H', imageio.imread(IMG_DIR / 'personH-img1.jpg')),
        ('Person H', imageio.imread(IMG_DIR / 'personH-img2.jpg')),
        ('Person J', imageio.imread(IMG_DIR / 'personJ-img1.jpg')),
        ('Person J', imageio.imread(IMG_DIR / 'personJ-img2.jpg')),
        ('Person J', imageio.imread(IMG_DIR / 'personJ-img3.jpg')),
        ('Person J', imageio.imread(IMG_DIR / 'personJ-img4.jpg'))

    ]
    return split_train_test(dataset_full)


def calculate_accuracy(model: ModelWrapperBase, dataset: Dataset) -> (int, int):
    undetected = 0
    for name, img in dataset.train:
        undetected += model.add_face_example(img, name)
    model.train()
    recognized = sum(name == model.recognize(img) for name, img in dataset.test)
    return recognized, undetected


if __name__ == '__main__':
    init_runtime()
    args = parse_args()
    model = EfrsRestApi(args.host) if args.host else EfrsLocal()
    dataset = get_test_dataset() if args.test else get_lfw_dataset()

    dataset_length = len(dataset.train)
    recognized_faces, undetected_faces = calculate_accuracy(model, dataset)
    detected_faces = dataset_length - undetected_faces
    detected_percent = (detected_faces / dataset_length) * 100
    total_recognized = round((recognized_faces / dataset_length) * 100, 1)
    recognized_from_detected = round((recognized_faces / detected_faces) * 100, 1)

    print(f'Training dataset: {dataset_length}\nUnique faces: {dataset_length}\nTest dataset: {dataset_length}\n'
          f'Detected faces: {detected_percent}% ({detected_faces}/{dataset_length})\n'
          f'Recognized faces: {recognized_from_detected}% ({recognized_faces}/{detected_faces})\n'
          f'-----\n'
          f'Recognized faces (total): {total_recognized}% ({recognized_faces}/{dataset_length})\n')
