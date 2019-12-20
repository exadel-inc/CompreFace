import imagehash
from PIL import Image

DIFFERENCE_THRESHOLD = 2


def images_are_almost_the_same(img1: Image, img2: Image):
    hash1 = imagehash.average_hash(img1)
    hash2 = imagehash.average_hash(img2)
    return hash1 - hash2 <= DIFFERENCE_THRESHOLD


def boxes_are_almost_the_same(box1, box2):
    xmin1, xmax1, ymin1, ymax1, probabitity1 = box1
    xmin2, xmax2, ymin2, ymax2, probabitity2 = box2
    return abs(xmax1 - xmax2) <= DIFFERENCE_THRESHOLD and abs(xmin1 - xmin2) <= DIFFERENCE_THRESHOLD and \
           abs(ymax1 - ymax2) <= DIFFERENCE_THRESHOLD and abs(ymin1 - ymin2) <= DIFFERENCE_THRESHOLD


def ndarray_to_img(ndarray) -> Image:
    return Image.fromarray((ndarray * 255).astype('uint8'), 'RGB')
