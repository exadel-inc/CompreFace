import numpy as np
import gzip
import requests
import os
from pathlib import Path
import numpy as np
from matplotlib.image import imread
import argparse
from src.init_runtime import init_runtime
from test_comp_models._model_wrappers import ModelWrapperBase, EfrsLocal_2018, EfrsLocal_InsightLib, EfrsRestApi_2018, \
    EfrsRestApi_Insightlib
from test_perf.dto import Dataset, Datarows
from typing import List, NamedTuple, Tuple
import math

CUR_DIR = Path(os.path.dirname(os.path.realpath(__file__)))


def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('--host')
    return parser.parse_args()


def create_folds():
    people_file = open(CUR_DIR / "people.txt", "rt")

    lines = people_file.readlines()
    first_fold = []
    second_fold = []
    third_fold = []
    forth_fold = []
    fifth_fold = []
    six_fold = []
    seventh_fold = []
    eight_fold = []
    nine_fold = []
    ten_fold = []

    list_of_folds = [first_fold, second_fold, third_fold, forth_fold, fifth_fold, six_fold, seventh_fold, eight_fold,
                     nine_fold, ten_fold]
    fold_count = 0
    for line in range(1, len(lines)):
        current = lines[line].split("\t")
        if len(current) == 1:
            fold = list_of_folds[fold_count]
            fold_count += 1
        else:
            if len(current[1].strip()) == 1:
                pic = str(current[0]) + "_000" + str(current[1].strip()) + ".jpg"
            elif len(current[1].strip()) == 2:
                pic = str(current[0]) + "_00" + str(current[1].strip()) + ".jpg"

            elif len(current[1].strip()) == 3:
                pic = str(current[0]) + "_0" + str(current[1].strip()) + ".jpg"

            elif len(current[1].strip()) == 4:
                pic = str(current[0]) + "_" + str(current[1].strip()) + ".jpg"
            img = CUR_DIR / "lfw" / current[0] / pic
            fold.append(Datarows(List[Tuple[current[0], imread(img)]]))
            # fold[current[0]] = imread(img)

    return first_fold, second_fold, third_fold, forth_fold, fifth_fold, six_fold, \
           seventh_fold, eight_fold, nine_fold, ten_fold


def calculate_accuracy(dataset: Dataset, model_1: ModelWrapperBase, model_2: ModelWrapperBase):
    len_trainset = len(dataset.train)
    undetected_1 = model_1.train(dataset.train)
    undetected_2 = model_2.train(dataset.train)
    detected_1 = len_trainset - undetected_1
    detected_2 = len_trainset - undetected_2
    detected_ratio_1 = detected_1 / len_trainset * 100
    detected_ratio_2 = detected_2 / len_trainset * 100

    len_testset = len(dataset.test)
    recognized_1 = model_1.predict(dataset.test)
    recognized_2 = model_2.predict(dataset.test)
    recognized_ratio_1 = recognized_1 / detected_1
    recognized_ratio_2 = recognized_2 / detected_2

    print("Model #1 detection " + detected_1 + "/" + len_trainset + "(" + detected_ratio_1 + "%")
    print("Model #2 detection " + detected_2 + "/" + len_trainset + "(" + detected_ratio_2 + "%")

    print("Model #1 detection " + recognized_1 + "/" + detected_1 + "(" + recognized_ratio_1 + "%)")
    print("Model #2 detection " + recognized_2 + "/" + detected_2 + "(" + recognized_ratio_2 + "%)")

    return recognized_ratio_1, detected_1, recognized_ratio_2, detected_2


if __name__ == '__main__':
    init_runtime()
    args = parse_args()
    if args.host:
        model_1 = EfrsRestApi_2018(args.host)
        model_2 = EfrsRestApi_Insightlib(args.host)
    else:
        model_1 = EfrsLocal_2018
        model_2 = EfrsLocal_InsightLib

    fold_1, fold_2, fold_3, fold_4, fold_5, fold_6, fold_7, fold_8, fold_9, fold_10 = create_folds()
    folds = [fold_1, fold_2, fold_3, fold_4, fold_5, fold_6, fold_7, fold_8, fold_9, fold_10]
    list_recognized_1 = []
    list_recognized_2 = []
    for experiment in range(len(folds)):
        train_set = []
        for fold in range(len(folds)):
            if fold == experiment:
                test_set = folds[fold]
            else:
                train_set.append(folds[fold])
        dataset = Dataset(train=train_set, test=test_set)
        print("fold #" + experiment)

        recognized_1, detected_1, recognized_2, detected_2 = calculate_accuracy(dataset, model_1, model_2)
        list_recognized_1.append(recognized_1)
        list_recognized_2.append(recognized_2)

    # idea: taking the size of the population as a 100, and then calculate accuracy based on that

    mean_1 = sum(list_recognized_1) / len(list_recognized_1)
    mean_2 = sum(list_recognized_2) / len(list_recognized_2)

    numerator_1 = 0
    numerator_2 = 0

    for i in range(len(list_recognized_1)):
        numerator_1 += (list_recognized_1[i] - mean_1) ** 2
        numerator_2 += (list_recognized_2[i] - mean_2) ** 2

    standard_deviation_1 = math.sqrt(numerator_1 / 100)
    standard_deviation_2 = math.sqrt(numerator_2 / 100)

