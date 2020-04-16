from collections import defaultdict, Counter

from src.services.facescan.compare._results import merge_results


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
