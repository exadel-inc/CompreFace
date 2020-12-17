from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

from datetime import datetime
import os
import numpy as np
import tensorflow as tf

from distutils.version import LooseVersion

VERSION_GTE_0_12_0 = LooseVersion(tf.__version__) >= LooseVersion('0.12.0')

# Name change in TF v 0.12.0
if VERSION_GTE_0_12_0:
    standardize_image = tf.image.per_image_standardization
else:
    standardize_image = tf.image.per_image_whitening

def data_files(data_dir, subset):
    """Returns a python list of all (sharded) data subset files.
    Returns:
      python list of all (sharded) data set files.
    Raises:
      ValueError: if there are not data_files matching the subset.
    """
    if subset not in ['train', 'validation']:
        print('Invalid subset!')
        exit(-1)

    tf_record_pattern = os.path.join(data_dir, '%s-*' % subset)
    data_files = tf.gfile.Glob(tf_record_pattern)
    print(data_files)
    if not data_files:
      print('No files found for data dir %s at %s' % (subset, data_dir))

      exit(-1)
    return data_files

def decode_jpeg(image_buffer, scope=None):
  """Decode a JPEG string into one 3-D float image Tensor.
  Args:
    image_buffer: scalar string Tensor.
    scope: Optional scope for op_scope.
  Returns:
    3-D float Tensor with values ranging from [0, 1).
  """
  with tf.op_scope([image_buffer], scope, 'decode_jpeg'):
    # Decode the string as an RGB JPEG.
    # Note that the resulting image contains an unknown height and width
    # that is set dynamically by decode_jpeg. In other words, the height
    # and width of image is unknown at compile-time.
    image = tf.image.decode_jpeg(image_buffer, channels=3)

    # After this point, all image pixels reside in [0,1)
    # until the very end, when they're rescaled to (-1, 1).  The various
    # adjust_* ops all require this range for dtype float.
    image = tf.image.convert_image_dtype(image, dtype=tf.float32)
    return image

def distort_image(image, height, width):

  # Image processing for training the network. Note the many random
  # distortions applied to the image.

  distorted_image = tf.random_crop(image, [height, width, 3])

  #distorted_image = tf.image.resize_images(image, [height, width])

  # Randomly flip the image horizontally.
  distorted_image = tf.image.random_flip_left_right(distorted_image)

  # Because these operations are not commutative, consider randomizing
  # the order their operation.

  distorted_image = tf.image.random_brightness(distorted_image,
                                               max_delta=63)

  distorted_image = tf.image.random_contrast(distorted_image,
                                             lower=0.2, upper=1.8)

  return distorted_image


def _is_tensor(x):
    return isinstance(x, (tf.Tensor, tf.Variable))

def eval_image(image, height, width):
    return tf.image.resize_images(image, [height, width])

def data_normalization(image):

    image = standardize_image(image)

    return image

def image_preprocessing(image_buffer, image_size, train, thread_id=0):
    """Decode and preprocess one image for evaluation or training.
    Args:
    image_buffer: JPEG encoded string Tensor
    train: boolean
    thread_id: integer indicating preprocessing thread
    Returns:
    3-D float Tensor containing an appropriately scaled image
    Raises:
    ValueError: if user does not provide bounding box
    """

    image = decode_jpeg(image_buffer)
    
    if train:
        image = distort_image(image, image_size, image_size)
    else:
        image = eval_image(image, image_size, image_size)
        
    image = data_normalization(image)
    return image


def parse_example_proto(example_serialized):
  # Dense features in Example proto.
  feature_map = {
      'image/encoded': tf.FixedLenFeature([], dtype=tf.string,
                                          default_value=''),
      'image/filename': tf.FixedLenFeature([], dtype=tf.string,
                                          default_value=''),

      'image/class/label': tf.FixedLenFeature([1], dtype=tf.int64,
                                              default_value=-1),
      'image/class/text': tf.FixedLenFeature([], dtype=tf.string,
                                             default_value=''),
      'image/height': tf.FixedLenFeature([1], dtype=tf.int64,
                                         default_value=-1),
      'image/width': tf.FixedLenFeature([1], dtype=tf.int64,
                                         default_value=-1),

  }

  features = tf.parse_single_example(example_serialized, feature_map)
  label = tf.cast(features['image/class/label'], dtype=tf.int32)
  return features['image/encoded'], label, features['image/filename']

def batch_inputs(data_dir, batch_size, image_size, train, num_preprocess_threads=4,
                 num_readers=1, input_queue_memory_factor=16):
  with tf.name_scope('batch_processing'):

    if train:
        files = data_files(data_dir, 'train')
        filename_queue = tf.train.string_input_producer(files,
                                                        shuffle=True,
                                                        capacity=16)
    else:
        files = data_files(data_dir, 'validation')
        filename_queue = tf.train.string_input_producer(files,
                                                        shuffle=False,
                                                        capacity=1)
    if num_preprocess_threads % 4:
              raise ValueError('Please make num_preprocess_threads a multiple '
                       'of 4 (%d % 4 != 0).', num_preprocess_threads)

    if num_readers < 1:
      raise ValueError('Please make num_readers at least 1')

    # Approximate number of examples per shard.
    examples_per_shard = 1024
    # Size the random shuffle queue to balance between good global
    # mixing (more examples) and memory use (fewer examples).
    # 1 image uses 299*299*3*4 bytes = 1MB
    # The default input_queue_memory_factor is 16 implying a shuffling queue
    # size: examples_per_shard * 16 * 1MB = 17.6GB
    min_queue_examples = examples_per_shard * input_queue_memory_factor
    if train:
      examples_queue = tf.RandomShuffleQueue(
          capacity=min_queue_examples + 3 * batch_size,
          min_after_dequeue=min_queue_examples,
          dtypes=[tf.string])
    else:
      examples_queue = tf.FIFOQueue(
          capacity=examples_per_shard + 3 * batch_size,
          dtypes=[tf.string])

    # Create multiple readers to populate the queue of examples.
    if num_readers > 1:
      enqueue_ops = []
      for _ in range(num_readers):
        reader = tf.TFRecordReader()
        _, value = reader.read(filename_queue)
        enqueue_ops.append(examples_queue.enqueue([value]))

      tf.train.queue_runner.add_queue_runner(
          tf.train.queue_runner.QueueRunner(examples_queue, enqueue_ops))
      example_serialized = examples_queue.dequeue()
    else:
      reader = tf.TFRecordReader()
      _, example_serialized = reader.read(filename_queue)

    images_labels_fnames = []
    for thread_id in range(num_preprocess_threads):
      # Parse a serialized Example proto to extract the image and metadata.
      image_buffer, label_index, fname = parse_example_proto(example_serialized)
          
      image = image_preprocessing(image_buffer, image_size, train, thread_id)
      images_labels_fnames.append([image, label_index, fname])

    images, label_index_batch, fnames = tf.train.batch_join(
        images_labels_fnames,
        batch_size=batch_size,
        capacity=2 * num_preprocess_threads * batch_size)

    images = tf.cast(images, tf.float32)
    images = tf.reshape(images, shape=[batch_size, image_size, image_size, 3])

    # Display the training images in the visualizer.
    tf.summary.image('images', images, 20)

    return images, tf.reshape(label_index_batch, [batch_size]), fnames

def inputs(data_dir, batch_size=128, image_size=227, train=False, num_preprocess_threads=4):
    with tf.device('/cpu:0'):
        images, labels, filenames = batch_inputs(
            data_dir, batch_size, image_size, train,
            num_preprocess_threads=num_preprocess_threads,
            num_readers=1)
    return images, labels, filenames

def distorted_inputs(data_dir, batch_size=128, image_size=227, num_preprocess_threads=4):

  # Force all input processing onto CPU in order to reserve the GPU for
  # the forward inference and back-propagation.
  with tf.device('/cpu:0'):
    images, labels, filenames = batch_inputs(
        data_dir, batch_size, image_size, train=True,
        num_preprocess_threads=num_preprocess_threads,
        num_readers=1)
  return images, labels, filenames
