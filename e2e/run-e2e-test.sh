#!/bin/bash -e
cd "${0%/*}" || exit 1 # Set Current Dir to the script's dir
python -m pytest -ra --verbose --disable-pytest-warnings ./src/e2e.py
