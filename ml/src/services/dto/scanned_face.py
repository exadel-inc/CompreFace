import colorsys
import random
from typing import List

import attr
from PIL import Image, ImageDraw, ImageFont

from src.services.dto.bounding_box import BoundingBox
from src.services.dto.json_encodable import JSONEncodable
from src.services.imgtools.proc_img import crop_img
from src.services.imgtools.types import Array1D, Array3D
from src.services.utils.pyutils import first_like_all


@attr.s(auto_attribs=True, frozen=True)
class ScannedFaceDTO(JSONEncodable):
    box: BoundingBox
    embedding: Array1D


class ScannedFace(JSONEncodable):
    def __init__(self, box: BoundingBox, embedding: Array1D, img: Array3D, face_img: Array3D = None):
        self.box = box
        self.embedding = embedding
        self.img = img
        self._face_img = face_img

    @property
    def face_img(self):
        if not self._face_img:
            self._face_img = crop_img(self.img, self.box)
        return self._face_img

    @property
    def dto(self):
        return ScannedFaceDTO(self.box, self.embedding)

    @staticmethod
    def show(scanned_faces: List['ScannedFace']):
        box_width = 3
        font_size = 20
        font = "arial"

        def get_random_bright_color():
            hsl = random.random(), 0.5 + random.random() / 2.0, 0.4 + random.random() / 5.0
            rgb = [int(256 * i) for i in colorsys.hls_to_rgb(*hsl)]
            return rgb

        img = first_like_all(f.img for f in scanned_faces)
        pil_img = Image.fromarray(img, 'RGB')
        draw = ImageDraw.Draw(pil_img)
        for i, face in enumerate(scanned_faces, start=1):
            color = get_random_bright_color()
            draw.rectangle(face.box.xy, outline=color, width=box_width)
            draw.text(text=str(i),
                      xy=(face.box.x_min, face.box.y_min - font_size),
                      fill=color, font=ImageFont.truetype(font, font_size))
            draw.text(text=f"{face.box.probability:.4f}",
                      xy=(face.box.x_min, face.box.y_min),
                      fill=color, font=ImageFont.truetype(font, font_size))
        pil_img.show()
