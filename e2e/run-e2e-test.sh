#!/bin/bash -e
cd "${0%/*}" || exit 1 # Set Current Dir to the script's dir

python -m pytest -s -ra --verbose --disable-pytest-warnings e2e_src/test/
