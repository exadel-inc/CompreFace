from src.constants import ENV_MAIN
from src.services.facescan.scanner.facescanner import MockScanner
from src.services.facescan.scanner.facescanners import TESTED_SCANNERS, id_2_face_scanner_cls
from src.services.utils.pyutils import Constants, get_env_split, get_env_bool


class ENV_BENCHMARK(Constants):
    SCANNERS = get_env_split('SCANNERS', ' '.join(s.ID for s in TESTED_SCANNERS))
    LOGGING_LEVEL_NAME = ENV_MAIN.LOGGING_LEVEL_NAME
    DRY_RUN = get_env_bool('DRY_RUN')


def get_scanner(scanner_name):
    if ENV_BENCHMARK.DRY_RUN:
        MockScanner.ID = scanner_name
        return MockScanner()
    return id_2_face_scanner_cls[scanner_name]()
