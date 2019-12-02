from typing import List

from skimage import transform

from src.face_recognition.crop_faces._detect_faces import detect_faces
from src.face_recognition.crop_faces._lib import facenet
from src.face_recognition.crop_faces.constants import IMAGE_SIZE, DEFAULT_THRESHOLD_C, FaceLimit, \
    FaceLimitConstant
from src.face_recognition.crop_faces.exceptions import IncorrectImageDimensionsError
from src.face_recognition.dto.cropped_face import CroppedFace, DetectedFace


def _preprocess_img(img):
    if img.ndim < 2:
        raise IncorrectImageDimensionsError("Unable to align image, it has only one dimension")
    img = facenet.to_rgb(img) if img.ndim == 2 else img
    img = img[:, :, 0:3]
    return img


def _crop_face_in_image(img, detected_face: DetectedFace) -> CroppedFace:
    box = detected_face.box
    cropped_img = img[box.ymin:box.ymax, box.xmin:box.xmax, :]
    resized_img = transform.resize(cropped_img, (IMAGE_SIZE, IMAGE_SIZE))
    return CroppedFace(img=resized_img, **detected_face.__dict__)


def crop_faces(img, face_limit: FaceLimit = FaceLimitConstant.NO_LIMIT,
               detection_threshold_c: float = DEFAULT_THRESHOLD_C) -> \
        List[CroppedFace]:
    img = _preprocess_img(img)
    detected_faces = detect_faces(img, face_limit, detection_threshold_c)
    cropped_faces = [_crop_face_in_image(img, detected_face) for detected_face in detected_faces]
    return cropped_faces


def crop_one_face(img, detection_threshold_c: float = DEFAULT_THRESHOLD_C) -> CroppedFace:
    cropped_faces = crop_faces(img, face_limit=1, detection_threshold_c=detection_threshold_c)
    return cropped_faces[0]
