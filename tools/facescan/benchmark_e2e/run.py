import logging

from ml.src.constants import ENV
from ml.src.init_runtime import init_runtime
from ml.src.services.facescan.scanner.facescanner import MockScanner
from ml.src.services.facescan.scanner.facescanners import id_2_face_scanner_cls
from tools.facescan.benchmark_e2e._dataset import get_lfw_dataset, get_people_txt_folds
from tools.facescan.benchmark_e2e._logistic_classifier_facade import LogisticClassifierFacade
from tools.facescan.benchmark_e2e._results import merge_results, results_to_str
from tools.facescan.benchmark_e2e.constants import _ENV, LOGGING_LEVEL

logger = logging.getLogger(__name__)


def _get_scanner(scanner_name):
    if _ENV.DRY_RUN:
        MockScanner.ID = scanner_name
        return MockScanner()
    return id_2_face_scanner_cls[scanner_name]()


if __name__ == '__main__':
    init_runtime(logging_level=LOGGING_LEVEL)
    logger.info(_ENV.to_json() if ENV.IS_DEV_ENV else _ENV.to_str())
    logging.getLogger('src.services.facescan.scanner').setLevel(logging.INFO)

    lfw_dataset = get_lfw_dataset()
    folds_images = get_people_txt_folds(lfw_dataset)

    for scanner_name in _ENV.SCANNERS:
        scanner = _get_scanner(scanner_name)
        results_per_fold = []
        for i, fold_images in enumerate(folds_images):
            train_dataset = lfw_dataset - fold_images
            test_dataset = fold_images

            tester = LogisticClassifierFacade(scanner)
            tester.train(train_dataset)
            results = tester.test_recognition(test_dataset)

            print(f'{scanner_name}, Fold #{i} {results_to_str(results)}', flush=True)
            results_per_fold.append(results)
        total_results = merge_results(results_per_fold)
        print(f'Total results for {scanner_name}: {results_to_str(total_results)}', flush=True)
