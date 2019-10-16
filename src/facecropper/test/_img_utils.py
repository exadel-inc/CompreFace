import imagehash
from PIL import Image

DIFFERENCE_THRESHOLD = 2


def images_are_the_same(img1: Image, img2: Image):
    hash1 = imagehash.average_hash(img1)
    hash2 = imagehash.average_hash(img2)
    return hash1 - hash2 <= DIFFERENCE_THRESHOLD


def ndarray_to_img(ndarray) -> Image:
    return Image.fromarray((ndarray * 255).astype('uint8'), 'RGB')
