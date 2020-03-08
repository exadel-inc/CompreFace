# import urllib
# import urllib.request
#
# import cv2
# import insightface
# import numpy as np
# from numpy.core.multiarray import ndarray
#
# from src.facescanner.dto.bounding_box import BoundingBox
# from src.facescanner.dto.embedding import Embedding
# from src.facescanner.dto.scanned_face import ScannedFace
#
#
# def scan_faces(img, face_limit=None, detection_threshold_c=None):
#     return [ScannedFace(box=BoundingBox(0, 0, 0, 0, 0), img=ndarray([1]),
#                         embedding=Embedding(array=ndarray([1]), calculator_version=''))]
#
#
# def url_to_image(url):
#     resp = urllib.request.urlopen(url)
#     image = np.asarray(bytearray(resp.read()), dtype="uint8")
#     image = cv2.imdecode(image, cv2.IMREAD_COLOR)
#     return image
