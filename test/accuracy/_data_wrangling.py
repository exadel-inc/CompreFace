import collections
import copy

import numpy as np

from test.test_perf.dto import Dataset, Datarows


def parse_lfw_data(lfw_people):
    count, img_height, img_width, img_colors = lfw_people.images.shape
    for i in range(count):
        name = lfw_people.target[i]
        image = lfw_people.images[i, :, :, :].reshape((img_height, img_width, img_colors)).astype(np.uint8)
        yield str(name), image


def split_train_test(dataset_full: Datarows):
    def tuples_to_dict(tuples):
        """
        >>> dict(tuples_to_dict([('Person A', 'img1'), ('Person A', 'img2'), ('Person B', 'img3')]))
        {'Person A': ['img1', 'img2'], 'Person B': ['img3']}
        """
        dict_ = collections.defaultdict(list)
        for k, v in tuples:
            dict_[k].append(v)
        return dict_

    def dict_to_tuples(dict_):
        """
        >>> dict(dict_to_tuples({'Person A': ['img1', 'img2'], 'Person B': ['img3']}))
        [('Person A', 'img1'), ('Person A', 'img2'), ('Person B', 'img3')]
        """
        return [(k, v) for k, v_list in dict_.items() for v in v_list]

    def split_dict_to_train_test(dict_):
        """
        >>> tuple(dict(d) for d in split_dict_to_train_test({'Person A': ['img1', 'img2'], 'Person B': ['img3']}))
        ({'Person A': ['img1'], 'Person B': ['img3']}, {'Person A': ['img2']})
        """
        dict_train = copy.deepcopy(dict_)
        dict_test = collections.defaultdict(list)
        for x in dict_train:
            if len(dict_train[x]) < 2:
                continue
            dict_test[x].append(dict_train[x].pop())
        return dict_train, dict_test

    dataset_dict = tuples_to_dict(dataset_full)
    dataset_dict_train, dataset_dict_test = split_dict_to_train_test(dataset_dict)
    dataset_train, dataset_test = dict_to_tuples(dataset_dict_train), dict_to_tuples(dataset_dict_test)
    return Dataset(train=dataset_train, test=dataset_test)
