import logging
from collections import namedtuple

from sample_images import IMG_DIR
from sample_images.annotations import SAMPLE_IMAGES
from src.init_runtime import init_runtime
from src.services.facescan.optimizer.random_optimizer import RandomOptimizer
from src.services.facescan.scanner.facescanners import FaceScanners
from src.services.facescan.scanner.test.calculate_errors import calculate_errors
from src.services.imgtools.read_img import read_img
from src.services.utils.pyutils import get_dir, cached, Constants, get_env

CURRENT_DIR = get_dir(__file__)
Score = namedtuple('Score', 'cost args')

cached_read_img = cached(read_img)


class _ENV(Constants):
    LOGGING_LEVEL_NAME = get_env('LOGGING_LEVEL_NAME', 'debug').upper()


LOGGING_LEVEL = logging._nameToLevel[_ENV.LOGGING_LEVEL_NAME]


class Facenet2018ThresholdOptimization:
    def __init__(self):
        self.scanner = FaceScanners.Facenet2018()
        self.dataset = [row for row in SAMPLE_IMAGES if row.img_name == '018_3.png']
        logging.info(f'dataset: {[r.img_name for r in self.dataset]}')
        logging.getLogger('src.services.facescan.scanner.test.calculate_errors').setLevel(logging.WARNING)
        logging.getLogger('src.services.facescan.scanner.facenet.facenet').setLevel(logging.INFO)

    def cost(self, new_x=None):
        if new_x:
            (self.scanner.det_prob_threshold,
             self.scanner.threshold_a,
             self.scanner.threshold_b,
             self.scanner.threshold_c) = tuple(new_x)

        total_errors = 0
        for row in self.dataset:
            img = cached_read_img(IMG_DIR / row.img_name)
            boxes = [face.box for face in self.scanner.scan(img)]
            errors = calculate_errors(boxes, row.noses)
            total_errors += errors
        return total_errors


if __name__ == '__main__':
    init_runtime(logging_level=LOGGING_LEVEL)
    logging.info(_ENV.__str__())
    optimizer = RandomOptimizer(Facenet2018ThresholdOptimization(),
                                arg_count=4,
                                arg_range=(0, 1),
                                checkpoint_filename='checkpoint.pickle',
                                checkpoint_every_s=20)
    optimizer.optimize()
