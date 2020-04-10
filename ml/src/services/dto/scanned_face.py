import colorsys
import logging
import random
from typing import List, Union

import attr
from PIL import Image, ImageDraw, ImageFont

from sample_images.annotations import name_2_annotation
from src.services.dto.bounding_box import BoundingBox
from src.services.dto.json_encodable import JSONEncodable
from src.services.facescan.imgscaler.imgscaler import ImgScaler
from src.services.imgtools.proc_img import crop_img
from src.services.imgtools.types import Array1D, Array3D
from src.services.utils.pyutils import first_like_all


@attr.s(auto_attribs=True, frozen=True)
class ScannedFaceDTO(JSONEncodable):
    box: BoundingBox
    embedding: Array1D


class ScannedFace(JSONEncodable):
    def __init__(self, box: BoundingBox, embedding: Array1D, img: Union[Array3D, None], face_img: Array3D = None):
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
        img_length_limit=700

        def random_bright_color_gen_cls():
            yield 0x8c, 0x3e, 0xd1
            yield 0x30, 0xa0, 0xf5
            yield 0x1e, 0xf3, 0x61
            yield 0xec, 0xfe, 0x0c
            yield 0xff, 0xad, 0x65
            yield 0xfd, 0x63, 0xb0
            while True:
                h, s, l_ = random.random(), 0.5 + random.random() / 2.0, 0.4 + random.random() / 5.0
                r, g, b = [int(256 * i) for i in colorsys.hls_to_rgb(h, s, l_)]
                yield r, g, b

        random_bright_color_gen = random_bright_color_gen_cls()

        def draw_dot(xy, radius, color):
            x, y = xy
            draw.ellipse((x - radius, y - radius, x + radius, y + radius), fill=color, outline=color)

        raw_img = first_like_all(f.img for f in scanned_faces)
        scaler = ImgScaler(img_length_limit)
        img = scaler.downscale_img(raw_img)
        pil_img = Image.fromarray(img, 'RGB')
        draw = ImageDraw.Draw(pil_img)
        sorted_scanned_faces = ScannedFace.sort_by_xy(scanned_faces)
        if hasattr(img, 'filename') and img.filename in name_2_annotation:
            noses = name_2_annotation[img.filename]
        else:
            logging.warning('[Annotation check] Annotation for image is not found')
            noses = []
        i = 0
        for i, face in enumerate(sorted_scanned_faces):
            color = next(random_bright_color_gen)
            box = scaler.downscale_box(face.box)
            draw.rectangle(box.xy, outline=color, width=box_width)
            nose = noses[i] if len(noses) > i else None
            if nose:
                draw_dot(xy=nose, radius=7, color=color)
            draw.text(text=str(i + 1),
                      xy=(box.x_min, box.y_min - font_size),
                      fill=color, font=ImageFont.truetype(font, font_size))
            draw.text(text=f"{box.probability:.4f}",
                      xy=(box.x_min, box.y_min),
                      fill=color, font=ImageFont.truetype(font, font_size))
        for j in range(i, len(noses) - 1):
            draw_dot(xy=noses[j], radius=30, color=next(random_bright_color_gen))
        pil_img.show()

    @staticmethod
    def sort_by_xy(scanned_faces: List['ScannedFace']):
        return sorted(scanned_faces, key=lambda f: [f.box.x_min, f.box.y_min])

    @classmethod
    def from_request(cls, result):
        box_result = result['box']
        return ScannedFace(box=BoundingBox(x_min=box_result['x_min'],
                                           x_max=box_result['x_max'],
                                           y_min=box_result['y_min'],
                                           y_max=box_result['y_max'],
                                           probability=box_result['probability']),
                           embedding=result['embedding'], img=None)
