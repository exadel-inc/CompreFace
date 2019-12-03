from typing import List

from numpy.core._multiarray_umath import ndarray

from src.scan_faces._calc_embedding.calculator import calculate_embedding
from src.scan_faces._detect_faces._lib import facenet
from src.scan_faces._detect_faces.constants import FaceLimit, FaceLimitConstant, DEFAULT_THRESHOLD_C
from src.scan_faces._detect_faces.detect_faces import detect_faces
from src.scan_faces._detect_faces.exceptions import IncorrectImageDimensionsError
from src.scan_faces.dto.cropped_face import ScannedFace, DetectedFace


def _preprocess_img(img: ndarray):
    if img.ndim < 2:
        raise IncorrectImageDimensionsError("Unable to align image, it has only one dimension")
    img = facenet.to_rgb(img) if img.ndim == 2 else img
    img = img[:, :, 0:3]
    return img


def get_scanned_face(img: ndarray, detected_face: DetectedFace) -> ScannedFace:
    embedding = calculate_embedding(img, detected_face.box)
    return ScannedFace(embedding=embedding, **detected_face.__dict__)


def scan_faces(img: ndarray, face_limit: FaceLimit = FaceLimitConstant.NO_LIMIT,
               detection_threshold_c: float = DEFAULT_THRESHOLD_C) -> \
        List[ScannedFace]:
    img = _preprocess_img(img)
    detected_faces = detect_faces(img, face_limit, detection_threshold_c)
    scanned_faces = [get_scanned_face(img, detected_face) for detected_face in detected_faces]
    return scanned_faces
