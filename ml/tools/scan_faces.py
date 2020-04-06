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
    scanner_id = get_env('SCANNER')
    image_name = get_env('IMG_NAME')
    do_show_img = get_env('SHOW_IMG', 'false').lower() in ('true', '1')
    scanned_faces = scan_faces(scanner_id, image_name)
    if do_show_img:
        ScannedFace.show(scanned_faces)
