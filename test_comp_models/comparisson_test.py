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
            #fold[current[0]] = imread(img)

    return first_fold, second_fold, third_fold, forth_fold, fifth_fold, six_fold, \
           seventh_fold, eight_fold, nine_fold, ten_fold



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
    for experiment in range(len(folds)):
        train_set = []
        for fold in range(len(folds)):
            if fold == experiment:
                test_set = folds[fold]
            else:
                train_set.append(folds[fold])
        dataset = Dataset(train=train_set, test=test_set)

