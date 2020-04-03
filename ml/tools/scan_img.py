import colorsys
import random
import sys
from typing import List

from PIL import Image, ImageFont, ImageDraw

from sample_images import IMG_DIR
from src.loggingw import init_logging
from src.services.dto.scanned_face import ScannedFace
from src.services.facescan.scanner.facescanner import FaceScanner
from src.services.facescan.scanner.facescanners import id_2_face_scanner_cls
from src.services.imgtools.read_img import read_img
from src.services.utils.pyutils import first_like_all

BOX_WIDTH = 3
FONT_SIZE = 20
FONT = "arial"


def get_random_bright_color():
    h, s, l = random.random(), 0.5 + random.random() / 2.0, 0.4 + random.random() / 5.0
    r, g, b = [int(256 * i) for i in colorsys.hls_to_rgb(h, l, s)]
    return r, g, b


def print_scanned_faces(scanned_faces: List[ScannedFace]):
    print(f'Found faces: {len(scanned_faces)}')
    for i, face in enumerate(scanned_faces, start=1):
        print(face.box)


def show_scanned_faces(scanned_faces: List[ScannedFace]):
    img = first_like_all(f.img for f in scanned_faces)
    pil_img = Image.fromarray(img, 'RGB')
    draw = ImageDraw.Draw(pil_img)
    for i, face in enumerate(scanned_faces, start=1):
        color = get_random_bright_color()
        draw.rectangle(face.box.xy, outline=color, width=BOX_WIDTH)
        draw.text(text=str(i),
                  xy=(face.box.x_min, face.box.y_min - FONT_SIZE),
                  fill=color, font=ImageFont.truetype(FONT, FONT_SIZE))
        draw.text(text=f"{face.box.probability:.4f}",
                  xy=(face.box.x_min, face.box.y_min),
                  fill=color, font=ImageFont.truetype(FONT, FONT_SIZE))
    pil_img.show()


if __name__ == '__main__':
    init_logging()
    scanner_id = sys.argv[1]  # e.g. 'Facenet2018'
    img_name = sys.argv[2]  # e.g. 'five-faces.jpg'
    img = read_img(IMG_DIR / img_name)
    scanner: FaceScanner = id_2_face_scanner_cls[scanner_id]()
    faces = scanner.scan(img)
    print_scanned_faces(faces)
    # show_scanned_faces(faces)
