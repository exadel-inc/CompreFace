import colorsys
import random
from typing import List, Tuple

from PIL import Image, ImageDraw, ImageFont
from scipy.spatial import distance

from ml.src.services.dto.bounding_box import BoundingBox
from ml.src.services.facescan.imgscaler.imgscaler import ImgScaler
from ml.src.services.imgtools.types import Array3D


def _random_bright_color_gen_cls():
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


def _draw_dot(draw, xy, radius, color):
    x, y = xy
    draw.ellipse((x - radius, y - radius, x + radius, y + radius),
                 fill=color, outline=color)


def _draw_cross(draw, xy, half_length, color, width):
    x, y = xy
    draw.line((x - half_length, y - half_length, x + half_length, y + half_length), fill=color, width=width)
    draw.line((x + half_length, y - half_length, x - half_length, y + half_length), fill=color, width=width)


def show_img(img: Array3D, boxes: List[BoundingBox] = None, noses: List[Tuple[int, int]] = None):
    box_line_width = 3
    font_size = 20
    font_size_smaller = 15
    radius = 7
    font = "arial"
    img_length_limit = 1200
    cross_half_length = 20
    error_color = 0xff, 0x44, 0x44
    error_line_width = 3

    def _draw_detection_box(text, box: BoundingBox, color):
        img_draw.rectangle(box.xy, outline=color, width=box_line_width)
        img_draw.text(text=text,
                      xy=(box.x_min, box.y_min - font_size - 1),
                      fill=color, font=ImageFont.truetype(font, font_size))
        img_draw.text(text=f"{box.probability:.4f}",
                      xy=(box.x_min, box.y_max + 1),
                      fill=color, font=ImageFont.truetype(font, font_size_smaller))

    scaler = ImgScaler(img_length_limit)
    img = scaler.downscale_img(img)
    pil_img = Image.fromarray(img, 'RGB')
    img_draw = ImageDraw.Draw(pil_img)
    noses_given = noses is not None
    noses = [scaler.downscale_nose(nose) for nose in noses or []]
    boxes = [scaler.downscale_box(box) for box in boxes or []]
    boxes = sorted(boxes, key=lambda box: (box.x_min, box.y_min))

    random_bright_color_gen = _random_bright_color_gen_cls()
    error_boxes = []
    i = 0
    for box in boxes:
        color = error_color
        dot_drawn = False
        if noses:
            # noinspection PyTypeChecker
            idx = distance.cdist([box.center], noses).argmin()
            nose = noses[idx]
            if box.is_point_inside(nose):
                color = next(random_bright_color_gen)
                _draw_dot(img_draw, xy=nose, radius=radius, color=color)
                noses.pop(idx)
                dot_drawn = True
        if noses_given and not dot_drawn:
            error_boxes.append(box)
            continue

        i += 1
        text = str(i)
        if not noses_given:
            color = next(random_bright_color_gen)
        _draw_detection_box(text, box, color)

    for box in error_boxes:
        color = error_color
        text = 'Error'
        _draw_detection_box(text, box, color)

    for nose in noses:
        _draw_cross(img_draw, xy=nose, half_length=cross_half_length, color=error_color, width=error_line_width)

    pil_img.show()
