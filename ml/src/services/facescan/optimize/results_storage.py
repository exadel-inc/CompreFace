from pathlib import Path

import joblib

CHECKPOINT_FILENAME = Path('tmp') / 'checkpoint.joblib'


class ResultsStorage:
    def __init__(self):
        self._scores = []
        self._total_scores = 0

    def save(self):
        self._scores = sorted(self._scores, key=lambda x: x.cost)[:100]
        joblib.dump(self._scores, CHECKPOINT_FILENAME)
        print(f"[Best out of {self._total_scores}]:"
              f" Cost = {self._scores[0].cost} <- {tuple(self._scores[0].args)}."
              f" Saved top 100 to '{CHECKPOINT_FILENAME}'.", flush=True)

    def add_score(self, score):
        self._scores.append(score)
        self._total_scores += 1
        score_count = len(self._scores)
        if score_count == 1 or score_count >= 5000:
            self.save()
