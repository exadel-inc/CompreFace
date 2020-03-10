import imagehash
from PIL import Image

DIFFERENCE_THRESHOLD = 2


def images_are_almost_the_same(img1: Image, img2: Image):
    hash1 = imagehash.average_hash(img1)
    hash2 = imagehash.average_hash(img2)
    return hash1 - hash2 <= DIFFERENCE_THRESHOLD


def boxes_are_almost_the_same(box1, box2):
    x_min1, x_max1, y_min1, y_max1, probabitity1 = box1
    x_min2, x_max2, y_min2, y_max2, probabitity2 = box2
    return abs(x_max1 - x_max2) <= DIFFERENCE_THRESHOLD and abs(x_min1 - x_min2) <= DIFFERENCE_THRESHOLD and \
           abs(y_max1 - y_max2) <= DIFFERENCE_THRESHOLD and abs(y_min1 - y_min2) <= DIFFERENCE_THRESHOLD


def ndarray_to_img(ndarray) -> Image:
    return Image.fromarray((ndarray * 255).astype('uint8'), 'RGB')
