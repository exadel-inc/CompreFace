import logging

from src.constants import ENV
from src.services.facescan.scanner.facescanners import TESTED_SCANNERS
from src.services.utils.pyutils import Constants, get_env_split, get_env_bool


class _ENV(Constants):
    SCANNERS = get_env_split('SCANNERS', ' '.join(s.ID for s in TESTED_SCANNERS))
    LOGGING_LEVEL_NAME = ENV.LOGGING_LEVEL_NAME
    DRY_RUN = get_env_bool('DRY_RUN')


LOGGING_LEVEL = logging._nameToLevel[ENV.LOGGING_LEVEL_NAME]
