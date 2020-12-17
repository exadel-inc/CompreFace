import numpy as np
import cv2
FACE_PAD = 50

class ObjectDetector(object):
    def __init__(self):
        pass

    def run(self, image_file):
        pass

# OpenCV's cascade object detector
class ObjectDetectorCascadeOpenCV(ObjectDetector):
    def __init__(self, model_name, basename='frontal-face', tgtdir='.', min_height_dec=20, min_width_dec=20,
                 min_height_thresh=50, min_width_thresh=50):
        self.min_height_dec = min_height_dec
        self.min_width_dec = min_width_dec
        self.min_height_thresh = min_height_thresh
        self.min_width_thresh = min_width_thresh
        self.tgtdir = tgtdir
        self.basename = basename
        self.face_cascade = cv2.CascadeClassifier(model_name)

    def run(self, image_file):
        print(image_file)
        img = cv2.imread(image_file)
        min_h = int(max(img.shape[0] / self.min_height_dec, self.min_height_thresh))
        min_w = int(max(img.shape[1] / self.min_width_dec, self.min_width_thresh))
        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        faces = self.face_cascade.detectMultiScale(gray, 1.3, minNeighbors=5, minSize=(min_h, min_w))

        images = []
        for i, (x, y, w, h) in enumerate(faces):
            images.append(self.sub_image('%s/%s-%d.jpg' % (self.tgtdir, self.basename, i + 1), img, x, y, w, h))

        print('%d faces detected' % len(images))

        for (x, y, w, h) in faces:
            self.draw_rect(img, x, y, w, h)
            # Fix in case nothing found in the image
        outfile = '%s/%s.jpg' % (self.tgtdir, self.basename)
        cv2.imwrite(outfile, img)
        return images, outfile

    def sub_image(self, name, img, x, y, w, h):
        upper_cut = [min(img.shape[0], y + h + FACE_PAD), min(img.shape[1], x + w + FACE_PAD)]
        lower_cut = [max(y - FACE_PAD, 0), max(x - FACE_PAD, 0)]
        roi_color = img[lower_cut[0]:upper_cut[0], lower_cut[1]:upper_cut[1]]
        cv2.imwrite(name, roi_color)
        return name

    def draw_rect(self, img, x, y, w, h):
        upper_cut = [min(img.shape[0], y + h + FACE_PAD), min(img.shape[1], x + w + FACE_PAD)]
        lower_cut = [max(y - FACE_PAD, 0), max(x - FACE_PAD, 0)]
        cv2.rectangle(img, (lower_cut[1], lower_cut[0]), (upper_cut[1], upper_cut[0]), (255, 0, 0), 2)

