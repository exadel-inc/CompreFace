import sys

from sample_images import IMG_DIR
from src.logging_ import init_runtime
from src.services.dto.scanned_face import ScannedFace
from src.services.facescan.scanner.facescanner import FaceScanner
from src.services.facescan.scanner.facescanners import id_2_face_scanner_cls
from src.services.imgtools.read_img import read_img
from src.services.utils.pyutils import get_env


def scan_faces(scanner_id, img_name):
    img = read_img(IMG_DIR / img_name)
    scanner: FaceScanner = id_2_face_scanner_cls[scanner_id]()
    return scanner.scan(img)


if __name__ == '__main__':
    init_runtime()
    do_show_img = get_env('SHOW_IMG', 'false').lower() in ('true', '1')
    scanner_id = sys.argv[1]  # e.g. 'Facenet2018'
    img_name = sys.argv[2]  # e.g. '00.x5.jpg'
    scanned_faces = scan_faces(scanner_id, img_name)
    if do_show_img:
        ScannedFace.show(scanned_faces)

