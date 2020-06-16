#  Copyright (c) 2020 the original author or authors
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       https://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
#  or implied. See the License for the specific language governing
#  permissions and limitations under the License.

import logging

from src.constants import ENV_MAIN, LOGGING_LEVEL
from src.init_runtime import init_runtime
from tools.facescan.benchmark_e2e._dataset import get_lfw_dataset, get_people_txt_folds
from tools.facescan.benchmark_e2e._logistic_classifier_facade import LogisticClassifierFacade
from tools.facescan.benchmark_e2e._results import merge_results, results_to_str
from tools.facescan.benchmark_e2e.constants import ENV
from tools.facescan.constants import get_scanner

logger = logging.getLogger(__name__)

if __name__ == '__main__':
    init_runtime(logging_level=LOGGING_LEVEL)
    logger.info(ENV.to_json() if ENV_MAIN.IS_DEV_ENV else ENV.to_str())
    logging.getLogger('src.services.facescan.scanner').setLevel(logging.INFO)

    lfw_dataset = get_lfw_dataset()
    folds_images = get_people_txt_folds(lfw_dataset)

    for scanner_name in ENV.SCANNERS:
        scanner = get_scanner(scanner_name)
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
