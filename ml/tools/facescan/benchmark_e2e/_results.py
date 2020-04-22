import json
from collections import Counter, defaultdict
from typing import List, Dict


def merge_results(results_list: List[Dict[str, Counter]]):
    returned_result = defaultdict(Counter)
    for results in results_list:
        for key in results:
            returned_result[key] += results[key]
    return returned_result


def results_to_str(results):
    return json.dumps(results, indent=4)
