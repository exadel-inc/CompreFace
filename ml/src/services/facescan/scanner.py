from src.services.facescan.backend.facenet.facenet import Facenet2018
from src.services.facescan.backend.insightface.insightface import InsightFace

ALL_BACKENDS = [Facenet2018, InsightFace]


class Scanner:
    """ Increases package usability """
    Facenet2017 = NotImplemented
    Facenet2018 = Facenet2018
    InsightFace = InsightFace
