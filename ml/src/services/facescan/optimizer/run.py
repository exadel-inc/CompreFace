import logging
from collections import namedtuple

from sample_images.annotations import SAMPLE_IMAGES
from src.logging_ import init_runtime
from src.services.facescan.optimizer.random_optimizer import RandomOptimizer
from src.services.facescan.scanner.facescanners import FaceScanners
from src.services.facescan.scanner.test.calculate_errors import calculate_errors
from src.services.utils.pyutils import get_dir

CURRENT_DIR = get_dir(__file__)
Score = namedtuple('Score', 'cost args')


class Facenet2018ThresholdOptimization:
    def __init__(self):
        self.scanner = FaceScanners.Facenet2018()
        self.dataset = SAMPLE_IMAGES
        logging.info(f'dataset: {[r.image_name for r in self.dataset]}')
        logging.getLogger('src.services.facescan.scanner.test.calculate_errors').setLevel(logging.WARNING)
        logging.getLogger('src.services.facescan.scanner.facenet.facenet').setLevel(logging.INFO)

    def cost(self, x=None):
        if x:
            self.scanner.threshold_a, self.scanner.threshold_b, self.scanner.threshold_c = tuple(x)
        return calculate_errors(self.scanner, self.dataset)


if __name__ == '__main__':
    init_runtime(logging_level=logging.DEBUG)
    optimizer = RandomOptimizer(Facenet2018ThresholdOptimization(),
                                arg_count=3,
                                arg_range=(0.5, 1),
                                checkpoint_filename='checkpoint.pickle',
                                checkpoint_n=1,
                                iter_n=1000000)
    optimizer.optimize()
