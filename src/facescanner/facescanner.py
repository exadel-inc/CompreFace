from typing import List

from numpy.core._multiarray_umath import ndarray

from src.facescanner._detector.constants import FaceLimit, FaceLimitConstant, DEFAULT_THRESHOLD_C
from src.facescanner._detector.detector import find_face_bounding_boxes
from src.facescanner._embedder.embedder import calculate_embedding
from src.facescanner._embedder.face_crop import crop_image
from src.facescanner.dto.scanned_face import ScannedFace


def scan_faces(img: ndarray,
               face_limit: FaceLimit = FaceLimitConstant.NO_LIMIT,
               detection_threshold_c: float = DEFAULT_THRESHOLD_C) -> List[ScannedFace]:
    scanned_faces = []
    for box in find_face_bounding_boxes(img, face_limit, detection_threshold_c):
        cropped_img = crop_image(img, box)
        embedding = calculate_embedding(cropped_img)
        scanned_faces.append(ScannedFace(img=cropped_img, embedding=embedding, box=box))
    return scanned_faces


def scan_face(img: ndarray,
              face_limit: FaceLimit = FaceLimitConstant.NO_LIMIT,
              detection_threshold_c: float = DEFAULT_THRESHOLD_C) -> ScannedFace:
    faces = scan_faces(img, face_limit, detection_threshold_c)
    assert len(faces) == 1
    return faces[0]
