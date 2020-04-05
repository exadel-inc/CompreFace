import logging
from abc import ABC, abstractmethod

import numpy as np
from scipy import optimize

from sample_images.annotations import SAMPLE_IMAGES
from src.logging_ import init_runtime
from src.services.facescan.scanner.facescanners import FaceScanners
from src.services.facescan.scanner.test.calculate_errors import calculate_errors


class OptimizationTask(ABC):
    @property
    @abstractmethod
    def x0(self):
        raise NotImplementedError

    @abstractmethod
    def cost(self, x):
        pass

    @abstractmethod
    def bounds(self, x_new, **kwargs):
        pass

    @abstractmethod
    def callback(self, x, f, accept):
        pass

""" Custom step-function """
class RandomDisplacementBounds(object):
    """random displacement with bounds:  see: https://stackoverflow.com/a/21967888/2320035
        Modified! (dropped acceptance-rejection sampling for a more specialized approach)
    """
    def __init__(self, xmin, xmax, stepsize=0.5):
        self.xmin = xmin
        self.xmax = xmax
        self.stepsize = stepsize

    def __call__(self, x):
        """take a random step but ensure the new position is within the bounds """
        min_step = np.maximum(self.xmin - x, -self.stepsize)
        max_step = np.minimum(self.xmax - x, self.stepsize)

        random_step = np.random.uniform(low=min_step, high=max_step, size=x.shape)
        xnew = x + random_step

        return xnew

class Facenet2018ThresholdOptimization(OptimizationTask):
    def __init__(self):
        self.scanner = FaceScanners.Facenet2018()
        self.dataset = [r for r in SAMPLE_IMAGES if r.image_name in ['01.A.jpg', '06.A.jpg', '08.B.jpg']]
        logging.info(f'dataset: {[r.image_name for r in self.dataset]}')
        logging.getLogger('src.services.facescan.scanner.test.calculate_errors').setLevel(logging.WARNING)
        logging.getLogger('src.services.facescan.scanner.facenet.facenet').setLevel(logging.INFO)

    @property
    def x0(self):
        s = FaceScanners.Facenet2018
        return np.array([s.DEFAULT_THRESHOLD_A, s.DEFAULT_THRESHOLD_B, s.DEFAULT_THRESHOLD_C])

    def cost(self, x):
        self.scanner.threshold_a, self.scanner.threshold_b, self.scanner.threshold_c = tuple(x)
        return calculate_errors(self.scanner, self.dataset)

    def bounds(self, x_new, **kwargs):
        return (0 <= x_new).all() and (x_new <= 1).all()

    def callback(self, x, f, accept):
        logging.debug(f"[{accept}] {f} <- {tuple(x)}")


if __name__ == '__main__':
    init_runtime()
    task = Facenet2018ThresholdOptimization()
    r = optimize.basinhopping(task.cost, x0=task.x0, accept_test=task.bounds, callback=task.callback)
    print(r)
