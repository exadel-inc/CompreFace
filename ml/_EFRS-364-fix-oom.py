import sys
from typing import List

import imageio
from PIL import Image, ImageFont, ImageDraw

from src.services.dto.scanned_face import ScannedFace
from src.services.facescan.backend.facescan_backend import FacescanBackend
from src.services.facescan.scanner import Scanner
from src.services.test.sample_images import IMG_DIR
from src.services.utils.pyutils import first_like_all

COLOR = (102, 255, 102)
BOX_WIDTH = 15
FONT_SIZE = 100
TEXT_Y_OFFSET = -100
FONT = "arial"


def show_scanned_faces(scanned_faces: List[ScannedFace]):
    img = first_like_all(f.img for f in scanned_faces)
    pil_img = Image.fromarray(img, 'RGB')
    draw = ImageDraw.Draw(pil_img)
    for i, face in enumerate(scanned_faces, start=1):
        draw.rectangle(face.box.xy, outline=COLOR, width=BOX_WIDTH)
        text = f"({i}) {face.box.probability}"
        text_location = (face.box.x_min, face.box.y_min + TEXT_Y_OFFSET)
        draw.text(text_location, text, fill=COLOR, font=ImageFont.truetype(FONT, FONT_SIZE))
    pil_img.show()


if __name__ == '__main__':
    filename = f'personD-img{sys.argv[2]}.jpg'
    scanner: FacescanBackend = {'fn': Scanner.Facenet2018, 'if': Scanner.InsightFace}[sys.argv[1]]()
    img = imageio.imread(IMG_DIR / filename)

    scanned_faces = scanner.scan(img)

    print(f'Found faces: {len(scanned_faces)}')
    for i, face in enumerate(scanned_faces, start=1):
        print(face.box)
    #show_scanned_faces(scanned_faces)
    # out = Scanner.InsightFace().scan(imageio.imread(IMG_DIR / 'personD-img2.jpg'))
