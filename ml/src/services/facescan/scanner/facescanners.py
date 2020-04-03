from src.services.facescan.scanner.facenet.facenet import Facenet2018
from src.services.facescan.scanner.insightface.insightface import InsightFace


class FaceScanners:
    """ Increases package usability """
    Facenet2018 = Facenet2018
    InsightFace = InsightFace


ALL_SCANNERS = [Facenet2018, InsightFace]
id_2_face_scanner_cls = {backend.ID: backend for backend in ALL_SCANNERS}
