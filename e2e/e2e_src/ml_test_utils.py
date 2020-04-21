from typing import List

from e2e_src import constants


def embeddings_are_the_same(embedding1: List[float], embedding2: List[float]):
    assert len(embedding1) == len(embedding2)
    for i in range(len(embedding1)):
        if (embedding1[i] - embedding2[i]) / embedding2[i] > constants.EMB_SIMILARITY_THRESHOLD:
            return False
    return True


def boxes_are_the_same(box1, box2):
    def value_is_the_same(key):
        return abs(box2[key] - box1[key]) <= constants.BBOX_ALLOWED_PX_DIFFERENCE

    return (value_is_the_same('x_max')
            and value_is_the_same('x_min')
            and value_is_the_same('y_max')
            and value_is_the_same('y_min'))
