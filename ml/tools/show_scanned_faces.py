import logging

from src.constants import ENV
from src.logging_ import init_runtime
from src.services.dto.scanned_face import ScannedFace
from src.services.utils.pyutils import get_env
from tools.scan_faces import scan_faces

if __name__ == '__main__':
    init_runtime()
    logging.info(ENV.__str__())
    scanner_id = get_env('SCANNER', 'Facenet2018')
    img_name = get_env('IMAGE_NAME', '00.x5.jpg')
    scanned_faces = scan_faces(scanner_id, img_name)
    ScannedFace.show(scanned_faces)
