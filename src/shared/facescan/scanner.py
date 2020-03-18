from src.shared.facescan.backend.facenet import Facenet2018, Facenet2017
from src.shared.facescan.backend.insightface.insightface import InsightFace

ALL_BACKENDS = [Facenet2018, InsightFace]


class Scanner:
    """ Increases package usability """
    Facenet2017 = Facenet2017
    Facenet2018 = Facenet2018
    InsightFace = InsightFace
