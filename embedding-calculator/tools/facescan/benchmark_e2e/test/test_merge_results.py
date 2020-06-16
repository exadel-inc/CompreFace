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

from collections import defaultdict, Counter

from tools.facescan.benchmark_e2e._results import merge_results


def test__when_merging_results__returns_merged_result():
    result1 = defaultdict(Counter)
    result2 = defaultdict(Counter)
    result1['A']['a'] += 1
    result2['A']['a'] += 2
    result1['new1']['a'] += 10
    result1['A']['new1'] += 100
    result2['new2']['a'] += 20
    result2['A']['new2'] += 200

    result = merge_results([result1, result2])

    assert result['A']['a'] == 3
    assert result['new1']['a'] == 10
    assert result['A']['new1'] == 100
    assert result['new2']['a'] == 20
    assert result['A']['new2'] == 200
