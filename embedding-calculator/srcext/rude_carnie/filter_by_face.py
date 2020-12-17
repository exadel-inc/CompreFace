import numpy as np
import tensorflow as tf
import os
import cv2
import time
import sys
from .utils import *
import csv

# YOLO tiny
#python fd.py --filename /media/dpressel/xdata/insights/converted/ --face_detection_model weights/YOLO_tiny.ckpt --face_detection_type yolo_tiny --target yolo.csv

# CV2

#python fd.py --filename /media/dpressel/xdata/insights/converted/ --face_detection_model /usr/share/opencv/haarcascades/haarcascade_frontalface_default.xml --target cascade.csv

tf.app.flags.DEFINE_string('filename', '',
                           'File (Image) or File list (Text/No header TSV) to process')

tf.app.flags.DEFINE_string('face_detection_model', '', 'Do frontal face detection with model specified')

tf.app.flags.DEFINE_string('face_detection_type', 'cascade', 'Face detection model type (yolo_tiny|cascade)')

tf.app.flags.DEFINE_string('target', None, 'Target file name (defaults to {face_detection_model}.csv')
FACE_PAD = 0
FLAGS = tf.app.flags.FLAGS

def list_images(srcfile):
    with open(srcfile, 'r') as csvfile:
        delim = ',' if srcfile.endswith('.csv') else '\t'
        reader = csv.reader(csvfile, delimiter=delim)
        if srcfile.endswith('.csv') or srcfile.endswith('.tsv'):
            print('skipping header')
            _ = next(reader)
        
        return [row[0] for row in reader]

def main(argv=None):  # pylint: disable=unused-argument

    fd = face_detection_model(FLAGS.face_detection_type, FLAGS.face_detection_model)
    files = []
    contains_faces = []

    target = FLAGS.target = '%s.csv' % FLAGS.face_detection_type if FLAGS.target is None else FLAGS.target

    print('Creating output file %s' % target)
    output = open(target, 'w')
    writer = csv.writer(output)
    writer.writerow(('file_with_face',))

    if FLAGS.filename is not None:
        if os.path.isdir(FLAGS.filename):
            for relpath in os.listdir(FLAGS.filename):
                abspath = os.path.join(FLAGS.filename, relpath)
                if os.path.isfile(abspath) and any([abspath.endswith('.' + ty) for ty in ('jpg', 'png', 'JPG', 'PNG', 'jpeg')]):
                    print(abspath)
                    files.append(abspath)
        elif any([FLAGS.filename.endswith('.' + ty) for ty in ('csv', 'tsv', 'txt')]):
            files = list_images(FLAGS.filename)
        else:
            files = [FLAGS.filename]

    for f in files:
        try:
            images, outfile = fd.run(f)
            if len(images):
                print(f, 'YES')
                writer.writerow((f,))
                contains_faces.append(f)
            else:
                print(f, 'NO')
        except Exception as e:
            print(e)

if __name__=='__main__':
    tf.app.run()
