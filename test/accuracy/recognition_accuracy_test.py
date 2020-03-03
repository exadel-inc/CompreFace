import argparse

import imageio
from sklearn.datasets import fetch_lfw_people

from main import ROOT_DIR
from src.init_runtime import init_runtime
from test.test_perf._data_wrangling import split_train_test, parse_lfw_data
from test.test_perf._model_wrappers import EfrsLocal, ModelWrapperBase, EfrsRestApi
from test.test_perf.dto import Dataset

IMG_DIR = ROOT_DIR / '_files'


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
        ('Person J', imageio.imread(IMG_DIR / 'personJ-img4.jpg')),
        ('No Face', imageio.imread(IMG_DIR / 'NoFace-img1.jpg')),
        ('No Face', imageio.imread(IMG_DIR / 'NoFace-img2.jpg'))

    ]
    return split_train_test(dataset_full)


def calculate_accuracy(model: ModelWrapperBase, dataset: Dataset) -> (int, int):
    undetected = 0
    list_to_remove = []
    unique_detection = 1
    name, img = dataset.train[0]
    prev_name = name
    for name, img in dataset.train:
        was_not_detected = model.add_face_example(img, name)
        undetected += was_not_detected
        if name != prev_name:
            unique_detection += 1

        if was_not_detected:
            list_to_remove.append(name)
        prev_name = name
    model.train()
    list_recognized = []
    name, img = dataset.test[0]
    prev_name = name
    unique_recognized = 1
    for name, img in dataset.test:
        if name in list_to_remove:
            dataset.test.remove((name, img))
        else:
            if name != prev_name:
                unique_recognized +=1
            list_recognized.append(name == model.recognize(img))
            prev_name = name
    recognized = sum(list_recognized)
    return recognized, undetected, unique_detection, unique_recognized


if __name__ == '__main__':
    init_runtime()
    args = parse_args()
    model = EfrsRestApi(args.host) if args.host else EfrsLocal()
    dataset = get_test_dataset() if args.test else get_lfw_dataset()

    dataset_length_total = len(dataset.train) + len(dataset.test)
    train_set_len = len(dataset.train)

    recognized_faces, undetected_faces, unique_detection_faces, unique_recognition_faces = calculate_accuracy(model, dataset)
    test_set_len = len(dataset.test)
    detected_faces = train_set_len - undetected_faces
    detected_percent = round((detected_faces / train_set_len) * 100, 1)
    total_recognized = round((recognized_faces / test_set_len) * 100, 1)
    recognized_from_detected = round((recognized_faces / detected_faces) * 100, 1)

    print(f'==================\n'
          f'Face detection performance:\n'
          f'Input dataset: {dataset_length_total} (Unique: {unique_detection_faces})\n'
          f'Detected faces: {detected_percent}% ({detected_faces}/{train_set_len})\n'
          f'\n'
          f'Face recognition performance:\n'
          f'Training dataset: {train_set_len} (Unique faces: {unique_detection_faces}), '
          f'test dataset: {test_set_len} (Unique faces: {unique_recognition_faces})\n'
          f'Recognized faces: {recognized_from_detected}% ({recognized_faces}/{detected_faces})\n')

