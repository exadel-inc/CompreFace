from detect import ObjectDetector

import numpy as np
import tensorflow as tf
import cv2

class YOLOBase(ObjectDetector):
    def __init__(self):
        pass

    def _conv_layer(self, idx, inputs, filters, size, stride):
        channels = inputs.get_shape()[3]
        weight = tf.Variable(tf.truncated_normal([size, size, int(channels), filters], stddev=0.1))
        biases = tf.Variable(tf.constant(0.1, shape=[filters]))

        pad_size = size // 2
        pad_mat = np.array([[0, 0], [pad_size, pad_size], [pad_size, pad_size], [0, 0]])
        inputs_pad = tf.pad(inputs, pad_mat)

        conv = tf.nn.conv2d(inputs_pad, weight, strides=[1, stride, stride, 1], padding='VALID',
                            name=str(idx) + '_conv')
        conv_biased = tf.add(conv, biases, name=str(idx) + '_conv_biased')
        return tf.maximum(self.alpha * conv_biased, conv_biased, name=str(idx) + '_leaky_relu')

    def _pooling_layer(self, idx, inputs, size, stride):
        return tf.nn.max_pool(inputs, ksize=[1, size, size, 1], strides=[1, stride, stride, 1], padding='SAME',
                              name=str(idx) + '_pool')

    def _fc_layer(self, idx, inputs, hiddens, flat=False, linear=False):
        input_shape = inputs.get_shape().as_list()
        if flat:
            dim = input_shape[1] * input_shape[2] * input_shape[3]
            inputs_transposed = tf.transpose(inputs, (0, 3, 1, 2))
            inputs_processed = tf.reshape(inputs_transposed, [-1, dim])
        else:
            dim = input_shape[1]
            inputs_processed = inputs
        weight = tf.Variable(tf.truncated_normal([dim, hiddens], stddev=0.1))
        biases = tf.Variable(tf.constant(0.1, shape=[hiddens]))
        if linear: return tf.add(tf.matmul(inputs_processed, weight), biases, name=str(idx) + '_fc')
        ip = tf.add(tf.matmul(inputs_processed, weight), biases)
        return tf.maximum(self.alpha * ip, ip, name=str(idx) + '_fc')

    def _init_base_model(self):
        self.x = tf.placeholder('float32', [None, 448, 448, 3])
        conv_1 = self._conv_layer(1, self.x, 16, 3, 1)
        pool_2 = self._pooling_layer(2, conv_1, 2, 2)
        conv_3 = self._conv_layer(3, pool_2, 32, 3, 1)
        pool_4 = self._pooling_layer(4, conv_3, 2, 2)
        conv_5 = self._conv_layer(5, pool_4, 64, 3, 1)
        pool_6 = self._pooling_layer(6, conv_5, 2, 2)
        conv_7 = self._conv_layer(7, pool_6, 128, 3, 1)
        pool_8 = self._pooling_layer(8, conv_7, 2, 2)
        conv_9 = self._conv_layer(9, pool_8, 256, 3, 1)
        pool_10 = self._pooling_layer(10, conv_9, 2, 2)
        conv_11 = self._conv_layer(11, pool_10, 512, 3, 1)
        pool_12 = self._pooling_layer(12, conv_11, 2, 2)
        conv_13 = self._conv_layer(13, pool_12, 1024, 3, 1)
        conv_14 = self._conv_layer(14, conv_13, 1024, 3, 1)
        conv_15 = self._conv_layer(15, conv_14, 1024, 3, 1)
        fc_16 = self._fc_layer(16, conv_15, 256, flat=True, linear=False)
        return self._fc_layer(17, fc_16, 4096, flat=False, linear=False)

    def _iou(self, box1, box2):
        tb = min(box1[0] + 0.5 * box1[2], box2[0] + 0.5 * box2[2]) - max(box1[0] - 0.5 * box1[2],
                                                                         box2[0] - 0.5 * box2[2])
        lr = min(box1[1] + 0.5 * box1[3], box2[1] + 0.5 * box2[3]) - max(box1[1] - 0.5 * box1[3],
                                                                         box2[1] - 0.5 * box2[3])
        if tb < 0 or lr < 0:
            intersection = 0
        else:
            intersection = tb * lr
        return intersection / (box1[2] * box1[3] + box2[2] * box2[3] - intersection)

    def sub_image(self, name, img, x, y, w, h):
        half_w = w // 2
        half_h = h // 2
        upper_cut = [y + half_h, x + half_w]
        lower_cut = [y - half_h, x - half_w];
        roi_color = img[lower_cut[0]:upper_cut[0], lower_cut[1]:upper_cut[1]]
        cv2.imwrite(name, roi_color)
        return name

    def draw_rect(self, img, x, y, w, h):
        half_w = w // 2
        half_h = h // 2
        upper_cut = [y + half_h, x + half_w]
        lower_cut = [y - half_h, x - half_w];
        cv2.rectangle(img, (lower_cut[1], lower_cut[0]), (upper_cut[1], upper_cut[0]), (0, 255, 0), 2)

    def run(self, filename):
        img = cv2.imread(filename)
        self.h_img, self.w_img, _ = img.shape
        img_resized = cv2.resize(img, (448, 448))
        img_RGB = cv2.cvtColor(img_resized, cv2.COLOR_BGR2RGB)
        img_resized_np = np.asarray(img_RGB)
        inputs = np.zeros((1, 448, 448, 3), dtype='float32')
        inputs[0] = (img_resized_np / 255.0) * 2.0 - 1.0
        in_dict = {self.x: inputs}
        net_output = self.sess.run(self.fc_19, feed_dict=in_dict)
        faces = self.interpret_output(net_output[0])
        images = []
        for i, (x, y, w, h, p) in enumerate(faces):
            images.append(self.sub_image('%s/%s-%d.jpg' % (self.tgtdir, self.basename, i + 1), img, x, y, w, h))

        print('%d faces detected' % len(images))

        for (x, y, w, h, p) in faces:
            print('Face found [%d, %d, %d, %d] (%.2f)' % (x, y, w, h, p));
            self.draw_rect(img, x, y, w, h)
            # Fix in case nothing found in the image
        outfile = '%s/%s.jpg' % (self.tgtdir, self.basename)
        cv2.imwrite(outfile, img)
        return images, outfile

    def __init__(self, model_name, basename, tgtdir, alpha, threshold, iou_threshold):
        self.alpha = alpha
        self.threshold = threshold
        self.iou_threshold = iou_threshold
        self.basename = basename
        self.tgtdir = tgtdir
        self.load_model(model_name)

class PersonDetectorYOLOTiny(YOLOBase):
    def __init__(self, model_name, basename='frontal-face', tgtdir='.', alpha=0.1, threshold=0.2, iou_threshold=0.5):
        self.alpha = alpha
        self.threshold = threshold
        self.iou_threshold = iou_threshold
        self.basename = basename
        self.tgtdir = tgtdir
        self.load_model(model_name)

    def load_model(self, model_name):
        g = tf.Graph()

        with g.as_default():
            fc_17 = self._init_base_model()
            # skip dropout_18
            self.fc_19 = self._fc_layer(19, fc_17, 1470, flat=False, linear=True)
            self.sess = tf.Session(graph=g)
            self.sess.run(tf.global_variables_initializer())
            self.saver = tf.train.Saver()
            self.saver.restore(self.sess, model_name)

    def interpret_output(self, output):
        probs = np.zeros((7, 7, 2, 20))
        class_probs = np.reshape(output[0:980], (7, 7, 20))
        scales = np.reshape(output[980:1078], (7, 7, 2))
        boxes = np.reshape(output[1078:], (7, 7, 2, 4))
        offset = np.transpose(np.reshape(np.array([np.arange(7)] * 14), (2, 7, 7)), (1, 2, 0))

        boxes[:, :, :, 0] += offset
        boxes[:, :, :, 1] += np.transpose(offset, (1, 0, 2))
        boxes[:, :, :, 0:2] = boxes[:, :, :, 0:2] / 7.0
        boxes[:, :, :, 2] = np.multiply(boxes[:, :, :, 2], boxes[:, :, :, 2])
        boxes[:, :, :, 3] = np.multiply(boxes[:, :, :, 3], boxes[:, :, :, 3])

        boxes[:, :, :, 0] *= self.w_img
        boxes[:, :, :, 1] *= self.h_img
        boxes[:, :, :, 2] *= self.w_img
        boxes[:, :, :, 3] *= self.h_img

        for i in range(2):
            for j in range(20):
                probs[:, :, i, j] = np.multiply(class_probs[:, :, j], scales[:, :, i])

        filter_mat_probs = np.array(probs >= self.threshold, dtype='bool')
        filter_mat_boxes = np.nonzero(filter_mat_probs)
        boxes_filtered = boxes[filter_mat_boxes[0], filter_mat_boxes[1], filter_mat_boxes[2]]
        probs_filtered = probs[filter_mat_probs]
        classes_num_filtered = np.argmax(filter_mat_probs, axis=3)[
            filter_mat_boxes[0], filter_mat_boxes[1], filter_mat_boxes[2]]

        argsort = np.array(np.argsort(probs_filtered))[::-1]
        boxes_filtered = boxes_filtered[argsort]
        probs_filtered = probs_filtered[argsort]
        classes_num_filtered = classes_num_filtered[argsort]

        for i in range(len(boxes_filtered)):
            if probs_filtered[i] == 0:
                continue

            for j in range(i + 1, len(boxes_filtered)):
                if self._iou(boxes_filtered[i], boxes_filtered[j]) > self.iou_threshold:
                    probs_filtered[j] = 0.0

        filter_iou = np.array(probs_filtered > 0.0, dtype='bool')
        boxes_filtered = boxes_filtered[filter_iou]
        probs_filtered = probs_filtered[filter_iou]
        classes_num_filtered = classes_num_filtered[filter_iou]

        result = []
        for i in range(len(boxes_filtered)):
            if classes_num_filtered[i] == 14:
                result.append([int(boxes_filtered[i][0]),
                               int(boxes_filtered[i][1]),
                               int(boxes_filtered[i][2]),
                               int(boxes_filtered[i][3]),
                               probs_filtered[i]])

        return result

# This model doesnt seem to work particularly well on data I have tried
class FaceDetectorYOLO(YOLOBase):
    def __init__(self, model_name, basename='frontal-face', tgtdir='.', alpha=0.1, threshold=0.2, iou_threshold=0.5):
        self.alpha = alpha
        self.threshold = threshold
        self.iou_threshold = iou_threshold
        self.basename = basename
        self.tgtdir = tgtdir
        self.load_model(model_name)

    def load_model(self, model_name):
        g = tf.Graph()

        with g.as_default():
            fc_17 = self._init_base_model()
            # skip dropout_18
            self.fc_19 = self._fc_layer(19, fc_17, 1331, flat=False, linear=True)
            self.sess = tf.Session(graph=g)
            self.sess.run(tf.global_variables_initializer())
            self.saver = tf.train.Saver()
            self.saver.restore(self.sess, model_name)

    def interpret_output(self, output):
        prob_range = [0, 11 * 11 * 1]
        scales_range = [prob_range[1], prob_range[1] + 11 * 11 * 2]
        boxes_range = [scales_range[1], scales_range[1] + 11 * 11 * 2 * 4]

        probs = np.zeros((11, 11, 2, 1))
        class_probs = np.reshape(output[0:prob_range[1]], (11, 11, 1))
        scales = np.reshape(output[scales_range[0]:scales_range[1]], (11, 11, 2))
        boxes = np.reshape(output[boxes_range[0]:], (11, 11, 2, 4))
        offset = np.transpose(np.reshape(np.array([np.arange(11)] * (2 * 11)), (2, 11, 11)), (1, 2, 0))

        boxes[:, :, :, 0] += offset
        boxes[:, :, :, 1] += np.transpose(offset, (1, 0, 2))
        boxes[:, :, :, 0:2] = boxes[:, :, :, 0:2] / float(11)
        boxes[:, :, :, 2] = np.multiply(boxes[:, :, :, 2], boxes[:, :, :, 2])
        boxes[:, :, :, 3] = np.multiply(boxes[:, :, :, 3], boxes[:, :, :, 3])

        boxes[:, :, :, 0] *= self.w_img
        boxes[:, :, :, 1] *= self.h_img
        boxes[:, :, :, 2] *= self.w_img
        boxes[:, :, :, 3] *= self.h_img

        for i in range(2):
            probs[:, :, i, 0] = np.multiply(class_probs[:, :, 0], scales[:, :, i])

        filter_mat_probs = np.array(probs >= self.threshold, dtype='bool')
        filter_mat_boxes = np.nonzero(filter_mat_probs)
        boxes_filtered = boxes[filter_mat_boxes[0], filter_mat_boxes[1], filter_mat_boxes[2]]
        probs_filtered = probs[filter_mat_probs]
        classes_num_filtered = np.argmax(filter_mat_probs, axis=3)[
            filter_mat_boxes[0], filter_mat_boxes[1], filter_mat_boxes[2]]

        argsort = np.array(np.argsort(probs_filtered))[::-1]
        boxes_filtered = boxes_filtered[argsort]
        probs_filtered = probs_filtered[argsort]
        classes_num_filtered = classes_num_filtered[argsort]

        for i in range(len(boxes_filtered)):
            if probs_filtered[i] == 0: continue
            for j in range(i + 1, len(boxes_filtered)):
                if self._iou(boxes_filtered[i], boxes_filtered[j]) > self.iou_threshold:
                    probs_filtered[j] = 0.0

        filter_iou = np.array(probs_filtered > 0.0, dtype='bool')
        boxes_filtered = boxes_filtered[filter_iou]
        probs_filtered = probs_filtered[filter_iou]
        classes_num_filtered = classes_num_filtered[filter_iou]

        result = []
        for i in range(len(boxes_filtered)):
            result.append([int(boxes_filtered[i][0]),
                           int(boxes_filtered[i][1]),
                           int(boxes_filtered[i][2]),
                           int(boxes_filtered[i][3]),
                           probs_filtered[i]])

        return result


