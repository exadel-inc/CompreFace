from src.services.utils.pyutils import cached

get_scanner = cached(lambda scanner_cls: scanner_cls())  # Singleton pattern
