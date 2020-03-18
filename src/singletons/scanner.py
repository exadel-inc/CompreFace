import src.shared
from src.shared.facescan.scanner import Scanner


@src.shared.utils.pyutils.run_once
def get_scanner():
    return Scanner.Facenet2018()
