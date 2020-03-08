from typing import List

from numpy.core._multiarray_umath import ndarray

from src.facescanner._detector._lib import facenet
from src.facescanner._detector.constants import FaceLimit, FaceLimitConstant, DEFAULT_THRESHOLD_C
from src.facescanner._detector.detector import find_face_bounding_boxes
from src.facescanner._detector.exceptions import IncorrectImageDimensionsError
from src.facescanner._embedder.embedder import calculate_embedding
from src.facescanner._embedder.face_crop import crop_image
from src.facescanner.dto.scanned_face import ScannedFace


# from src.facescanner_insightface.facescanner import scan_faces as scan_faces_insightface


def _preprocess_img(img: ndarray):
    if img.ndim < 2:
        raise IncorrectImageDimensionsError("Unable to align image, it has only one dimension")
    img = facenet.to_rgb(img) if img.ndim == 2 else img
    img = img[:, :, 0:3]
    return img


def scan_faces(img: ndarray,
               face_limit: FaceLimit = FaceLimitConstant.NO_LIMIT,
               detection_threshold_c: float = DEFAULT_THRESHOLD_C) -> List[ScannedFace]:
    scanned_faces = []
    img = _preprocess_img(img)
    for box in find_face_bounding_boxes(img, face_limit, detection_threshold_c):
        cropped_img = crop_image(img, box)
        embedding = calculate_embedding(cropped_img)
        scanned_faces.append(ScannedFace(img=cropped_img, embedding=embedding, box=box))
    return scanned_faces


# scan_faces = scan_faces_insightface


def scan_face(img: ndarray, detection_threshold_c: float = DEFAULT_THRESHOLD_C) -> ScannedFace:
    faces = scan_faces(img, detection_threshold_c=detection_threshold_c)
    assert len(faces) == 1
    return faces[0]
