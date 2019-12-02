import logging

from sklearn.datasets import fetch_lfw_people

from src.face_recognition.crop_faces.exceptions import NoFaceFoundError
from src.init_runtime import init_runtime
from test_perf._data_wrangling import split_train_test, parse_lfw_data
from test_perf._model_wrappers import PythonModel, ModelWrapperBase
from test_perf.dto import Dataset


def get_lfw_dataset() -> Dataset:
    lfw_data = fetch_lfw_people(min_faces_per_person=120, color=True, funneled=False)
    dataset_full = parse_lfw_data(lfw_data)
    return split_train_test(dataset_full)


def calculate_accuracy(model: ModelWrapperBase, dataset: Dataset) -> float:
    for name, img in dataset.train:
        try:
            model.add_example(img, name)
        except NoFaceFoundError as e:
            logging.warning(str(e))
    model.train()
    return sum(name == model.recognize(img) for name, img in dataset.test) / len(dataset.test)


if __name__ == '__main__':
    init_runtime()
    accuracy = calculate_accuracy(model=PythonModel(), dataset=get_lfw_dataset())
    print(f'Accuracy: {accuracy * 100}%')
