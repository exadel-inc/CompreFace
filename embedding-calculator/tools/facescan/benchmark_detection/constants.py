from src.services.utils.pyutils import get_env_bool
from tools.facescan.constants import ENV_BENCHMARK


class ENV(ENV_BENCHMARK):
    SAVE_IMG_ON_ERROR = get_env_bool('SAVE_IMG_ON_ERROR', default=True) and not ENV_BENCHMARK.DRY_RUN
