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

import time
from pathlib import Path

import joblib


class ResultsStorage:
    def __init__(self):
        self._scores = []
        self._total_scores = 0
        timestamp_ms = int(round(time.time() * 1000))
        self._checkpoint_filename = Path('tmp') / f'scores_top100_{timestamp_ms}.joblib'

    def save(self):
        self._scores = sorted(self._scores, key=lambda x: x.cost)[:100]
        joblib.dump(self._scores, self._checkpoint_filename)
        print(f"[Best out of {self._total_scores}]:"
              f" Cost = {self._scores[0].cost} <- {tuple(self._scores[0].args)}."
              f" Saved top 100 to '{self._checkpoint_filename}'.", flush=True)

    def add_score(self, score):
        self._scores.append(score)
        self._total_scores += 1
        score_count = len(self._scores)
        if score_count == 1 or score_count >= 5000:
            self.save()
