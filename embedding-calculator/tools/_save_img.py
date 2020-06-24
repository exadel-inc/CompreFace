#  Copyright (c) 2020 the original author or authors
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       https://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
#  or implied. See the License for the specific language governing
#  permissions and limitations under the License.

import colorsys
import logging
import random
import string
from pathlib import Path
from typing import List, Tuple, Union

from PIL import Image, ImageDraw, ImageFont
from colour import Color

from src.services.dto.bounding_box import BoundingBoxDTO
from src.services.facescan.imgscaler.imgscaler import ImgScaler
from src.services.imgtools.types import Array3D
from src.services.utils.pyutils import get_current_dir, get_nearest_point_idx

logger = logging.getLogger(__name__)
TMP_DIR = get_current_dir(__file__) / 'tmp'


# noinspection PyTypeChecker
def _to_rgb255(color: Color) -> Tuple[int, int, int]:
    """
    >>> _to_rgb255(Color('white'))
    (255, 255, 255)
    """
    return tuple(round(c * 255) for c in color.rgb)


def _bright_color_gen():
    yield _to_rgb255(Color('#8c3ed1'))
    yield _to_rgb255(Color('#30a0f5'))
    yield _to_rgb255(Color('#1ef361'))
    yield _to_rgb255(Color('#ecfe0c'))
    yield _to_rgb255(Color('#ffad65'))
    yield _to_rgb255(Color('#fd63b0'))
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


def _get_font(size):
    try:
        font_path = str(TMP_DIR / 'arimo-font' / 'Arimo-Regular.ttf')
        return ImageFont.truetype(font_path, size)
    except Exception as e:
        logger.debug(f"Using raster font because couldn't get custom font: {str(e)}")
        return ImageFont.load_default()


def _get_filepath(filepath):
    _filename = ''.join(random.choice(string.ascii_letters) for i in range(16))
    if isinstance(filepath, str):
        _filename, filepath = filepath, None
    return filepath or TMP_DIR / f"{_filename}.png"


def save_img(img: Array3D,
             boxes: List[BoundingBoxDTO] = None,
             noses: List[Tuple[int, int]] = None,
             filepath: Union[Path, str] = None):
    filepath = _get_filepath(filepath)
    box_line_width = 3
    font_size = 20
    font_size_smaller = 15
    radius = 7
    img_length_limit = 1200
    cross_half_length = 20
    green_color = _to_rgb255(Color('#36cc36'))
    error_color = _to_rgb255(Color('#ff4444'))
    error_line_width = 3

    def _draw_detection_box(text, box: BoundingBoxDTO, color):
        img_draw.rectangle(box.xy, outline=color, width=box_line_width)
        img_draw.text(text=text,
                      xy=(box.x_min, box.y_min - font_size - 1),
                      fill=color, font=_get_font(font_size))
        img_draw.text(text=f"{box.probability:.4f}",
                      xy=(box.x_min, box.y_max + 1),
                      fill=color, font=_get_font(font_size_smaller))

    scaler = ImgScaler(img_length_limit)
    img = scaler.downscale_img(img)
    pil_img = Image.fromarray(img, 'RGB')
    img_draw = ImageDraw.Draw(pil_img)
    noses_given = noses is not None
    noses = [scaler.downscale_nose(nose) for nose in noses or ()]
    boxes = [box.scaled(scaler.downscale_coefficient) for box in boxes or ()]
    boxes = sorted(boxes, key=lambda box: (box.x_min, box.y_min))

    draw_boxes = []
    draw_error_boxes = []
    draw_boxnoses = []
    for box in boxes:
        dot_drawn = False
        if noses:
            nearest_nose_idx = get_nearest_point_idx(box.center, noses)
            nearest_nose = noses[nearest_nose_idx]
            if box.is_point_inside(nearest_nose):
                draw_boxnoses.append((box, nearest_nose))
                noses.pop(nearest_nose_idx)
                dot_drawn = True
        if noses_given and not dot_drawn:
            draw_error_boxes.append(box)
        if not noses_given:
            draw_boxes.append(box)

    color_iter = _bright_color_gen()
    no_errors_found = len(noses) == 0 and len(draw_error_boxes) == 0
    for i, boxnose in enumerate(draw_boxnoses):
        box, nose = boxnose
        color = next(color_iter) if no_errors_found else green_color
        _draw_detection_box(text=str(i + 1), box=box, color=color)
        _draw_dot(img_draw, xy=nose, radius=radius, color=color)
    for i, box in enumerate(draw_boxes):
        color = next(color_iter) if no_errors_found else green_color
        _draw_detection_box(text=str(i + 1), box=box, color=color)
    for box in draw_error_boxes:
        _draw_detection_box(text='Error', box=box, color=error_color)
    for nose in noses:
        _draw_cross(img_draw, xy=nose, half_length=cross_half_length, color=error_color, width=error_line_width)

    pil_img.save(filepath, 'PNG')
