import src.shared.utils.pyutils

get_scanner = src.shared.utils.pyutils.cached(lambda scanner_cls: scanner_cls())  # Singleton pattern
