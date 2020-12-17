from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

import six.moves
from datetime import datetime
import sys
import math
import time
from .data import inputs, standardize_image
import numpy as np
import tensorflow as tf
from detect import *
import re

RESIZE_AOI = 256
RESIZE_FINAL = 227

# Modifed from here
# http://stackoverflow.com/questions/3160699/python-progress-bar#3160819
class ProgressBar(object):
    DEFAULT = 'Progress: %(bar)s %(percent)3d%%'
    FULL = '%(bar)s %(current)d/%(total)d (%(percent)3d%%) %(remaining)d to go'

    def __init__(self, total, width=40, fmt=DEFAULT, symbol='='):
        assert len(symbol) == 1

        self.total = total
        self.width = width
        self.symbol = symbol
        self.fmt = re.sub(r'(?P<name>%\(.+?\))d',
            r'\g<name>%dd' % len(str(total)), fmt)

        self.current = 0

    def update(self, step=1):
        self.current += step
        percent = self.current / float(self.total)
        size = int(self.width * percent)
        remaining = self.total - self.current
        bar = '[' + self.symbol * size + ' ' * (self.width - size) + ']'

        args = {
            'total': self.total,
            'bar': bar,
            'current': self.current,
            'percent': percent * 100,
            'remaining': remaining
        }
        six.print_('\r' + self.fmt % args, end='')

    def done(self):
        self.current = self.total
        self.update(step=0)
        print('')

# Read image files            
class ImageCoder(object):
    
    def __init__(self):
        # Create a single Session to run all image coding calls.
        config = tf.ConfigProto(allow_soft_placement=True)
        self._sess = tf.Session(config=config)
        
        # Initializes function that converts PNG to JPEG data.
        self._png_data = tf.placeholder(dtype=tf.string)
        image = tf.image.decode_png(self._png_data, channels=3)
        self._png_to_jpeg = tf.image.encode_jpeg(image, format='rgb', quality=100)
        
        # Initializes function that decodes RGB JPEG data.
        self._decode_jpeg_data = tf.placeholder(dtype=tf.string)
        self._decode_jpeg = tf.image.decode_jpeg(self._decode_jpeg_data, channels=3)
        self.crop = tf.image.resize_images(self._decode_jpeg, (RESIZE_AOI, RESIZE_AOI))

    def png_to_jpeg(self, image_data):
        return self._sess.run(self._png_to_jpeg,
                              feed_dict={self._png_data: image_data})
        
    def decode_jpeg(self, image_data):
        image = self._sess.run(self.crop, #self._decode_jpeg,
                               feed_dict={self._decode_jpeg_data: image_data})

        assert len(image.shape) == 3
        assert image.shape[2] == 3
        return image
        

def _is_png(filename):
    """Determine if a file contains a PNG format image.
    Args:
    filename: string, path of the image file.
    Returns:
    boolean indicating if the image is a PNG.
    """
    return '.png' in filename
        
def make_multi_image_batch(filenames, coder):
    """Process a multi-image batch, each with a single-look
    Args:
    filenames: list of paths
    coder: instance of ImageCoder to provide TensorFlow image coding utils.
    Returns:
    image_buffer: string, JPEG encoding of RGB image.
    """

    images = []

    for filename in filenames:
        with tf.gfile.FastGFile(filename, 'rb') as f:
            image_data = f.read()
        # Convert any PNG to JPEG's for consistency.
        if _is_png(filename):
            print('Converting PNG to JPEG for %s' % filename)
            image_data = coder.png_to_jpeg(image_data)
    
        image = coder.decode_jpeg(image_data)

        crop = tf.image.resize_images(image, (RESIZE_FINAL, RESIZE_FINAL))
        image = standardize_image(crop)
        images.append(image)
    image_batch = tf.stack(images)
    return image_batch

def make_multi_crop_batch(filename, coder):
    """Process a single image file.
    Args:
    filename: string, path to an image file e.g., '/path/to/example.JPG'.
    coder: instance of ImageCoder to provide TensorFlow image coding utils.
    Returns:
    image_buffer: string, JPEG encoding of RGB image.
    """
    # Read the image file.
    with tf.gfile.FastGFile(filename, 'rb') as f:
        image_data = f.read()

    # Convert any PNG to JPEG's for consistency.
    if _is_png(filename):
        print('Converting PNG to JPEG for %s' % filename)
        image_data = coder.png_to_jpeg(image_data)
    
    image = coder.decode_jpeg(image_data)

    crops = []
    print('Running multi-cropped image')
    h = image.shape[0]
    w = image.shape[1]
    hl = h - RESIZE_FINAL
    wl = w - RESIZE_FINAL

    crop = tf.image.resize_images(image, (RESIZE_FINAL, RESIZE_FINAL))
    crops.append(standardize_image(crop))
    crops.append(standardize_image(tf.image.flip_left_right(crop)))

    corners = [ (0, 0), (0, wl), (hl, 0), (hl, wl), (int(hl/2), int(wl/2))]
    for corner in corners:
        ch, cw = corner
        cropped = tf.image.crop_to_bounding_box(image, ch, cw, RESIZE_FINAL, RESIZE_FINAL)
        crops.append(standardize_image(cropped))
        flipped = standardize_image(tf.image.flip_left_right(cropped))
        crops.append(standardize_image(flipped))

    image_batch = tf.stack(crops)
    return image_batch



def face_detection_model(model_type, model_path):
    model_type_lc = model_type.lower()
    if model_type_lc == 'yolo_tiny':
        from yolodetect import PersonDetectorYOLOTiny
        return PersonDetectorYOLOTiny(model_path)
    elif model_type_lc == 'yolo_face':
        from yolodetect import FaceDetectorYOLO
        return FaceDetectorYOLO(model_path)
    elif model_type == 'dlib':
        from dlibdetect import FaceDetectorDlib
        return FaceDetectorDlib(model_path)
    return ObjectDetectorCascadeOpenCV(model_path)
