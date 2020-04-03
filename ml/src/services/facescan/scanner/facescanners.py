from src.services.facescan.scanner.facenet.facenet import Facenet2018
from src.services.facescan.scanner.facescanner import ALL_SCANNERS
from src.services.facescan.scanner.insightface.insightface import InsightFace


class FaceScanners:
    """ Increases package usability """
    Facenet2018 = Facenet2018
    InsightFace = InsightFace


Scanners = {backend.ID: backend for backend in ALL_SCANNERS}
