import tensorflow as tf
from .model import select_model, get_checkpoint
from .utils import RESIZE_AOI, RESIZE_FINAL
from tensorflow.python.framework import graph_util
from tensorflow.contrib.learn.python.learn.utils import export
from tensorflow.python.saved_model import builder as saved_model_builder
from tensorflow.python.saved_model import signature_constants
from tensorflow.python.saved_model import signature_def_utils
from tensorflow.python.saved_model import tag_constants
from tensorflow.python.saved_model import utils

import os

GENDER_LIST =['M','F']
AGE_LIST = ['(0, 2)','(4, 6)','(8, 12)','(15, 20)','(25, 32)','(38, 43)','(48, 53)','(60, 100)']

tf.app.flags.DEFINE_string('checkpoint', 'checkpoint',
                           'Checkpoint basename')

tf.app.flags.DEFINE_string('class_type', 'age',
                           'Classification type (age|gender)')

tf.app.flags.DEFINE_string('model_dir', '',
                           'Model directory (where training data lives)')

tf.app.flags.DEFINE_integer('model_version', 1,
                            """Version number of the model.""")

tf.app.flags.DEFINE_string('output_dir', '/tmp/tf_exported_model/0',
                           'Export directory')

tf.app.flags.DEFINE_string('model_type', 'default',
                           'Type of convnet')

tf.app.flags.DEFINE_string('requested_step', '', 'Within the model directory, a requested step to restore e.g., 9000')

FLAGS = tf.app.flags.FLAGS

def preproc_jpeg(image_buffer):
    image = tf.image.decode_jpeg(image_buffer, channels=3)
    crop = tf.image.resize_images(image, (RESIZE_AOI, RESIZE_AOI))
    # What??
    crop = tf.image.resize_images(crop, (RESIZE_FINAL, RESIZE_FINAL))    
    image_out = tf.image.per_image_standardization(crop)
    return image_out

def main(argv=None):
    with tf.Graph().as_default():

        serialized_tf_example = tf.placeholder(tf.string, name='tf_example')
        feature_configs = {
            'image/encoded': tf.FixedLenFeature(shape=[], dtype=tf.string),
        }
        tf_example = tf.parse_example(serialized_tf_example, feature_configs)
        jpegs = tf_example['image/encoded']

        images = tf.map_fn(preproc_jpeg, jpegs, dtype=tf.float32)
        label_list = AGE_LIST if FLAGS.class_type == 'age' else GENDER_LIST
        nlabels = len(label_list)

        config = tf.ConfigProto(allow_soft_placement=True)
        with tf.Session(config=config) as sess:

            model_fn = select_model(FLAGS.model_type)
            logits = model_fn(nlabels, images, 1, False)
            softmax_output = tf.nn.softmax(logits)
            values, indices = tf.nn.top_k(softmax_output, 2 if FLAGS.class_type == 'age' else 1)
            class_tensor = tf.constant(label_list)
            table = tf.contrib.lookup.index_to_string_table_from_tensor(class_tensor)
            classes = table.lookup(tf.to_int64(indices))
            requested_step = FLAGS.requested_step if FLAGS.requested_step else None
            checkpoint_path = '%s' % (FLAGS.model_dir)
            model_checkpoint_path, global_step = get_checkpoint(checkpoint_path, requested_step, FLAGS.checkpoint)

            saver = tf.train.Saver()
            saver.restore(sess, model_checkpoint_path)
            print('Restored model checkpoint %s' % model_checkpoint_path)

            output_path = os.path.join(
                tf.compat.as_bytes(FLAGS.output_dir),
                tf.compat.as_bytes(str(FLAGS.model_version)))
            print('Exporting trained model to %s' % output_path)
            builder = tf.saved_model.builder.SavedModelBuilder(output_path)

            # Build the signature_def_map.
            classify_inputs_tensor_info = tf.saved_model.utils.build_tensor_info(
                serialized_tf_example)
            classes_output_tensor_info = tf.saved_model.utils.build_tensor_info(
                classes)
            scores_output_tensor_info = tf.saved_model.utils.build_tensor_info(values)
            classification_signature = (
                tf.saved_model.signature_def_utils.build_signature_def(
                    inputs={
                        tf.saved_model.signature_constants.CLASSIFY_INPUTS:
                    classify_inputs_tensor_info
                    },
                    outputs={
                    tf.saved_model.signature_constants.CLASSIFY_OUTPUT_CLASSES:
                        classes_output_tensor_info,
                        tf.saved_model.signature_constants.CLASSIFY_OUTPUT_SCORES:
                        scores_output_tensor_info
                    },
                    method_name=tf.saved_model.signature_constants.
                    CLASSIFY_METHOD_NAME))
            
            predict_inputs_tensor_info = tf.saved_model.utils.build_tensor_info(jpegs)
            prediction_signature = (
                tf.saved_model.signature_def_utils.build_signature_def(
                    inputs={'images': predict_inputs_tensor_info},
                    outputs={
                        'classes': classes_output_tensor_info,
                        'scores': scores_output_tensor_info
                    },
                    method_name=tf.saved_model.signature_constants.PREDICT_METHOD_NAME
                ))
            
            legacy_init_op = tf.group(tf.tables_initializer(), name='legacy_init_op')
            builder.add_meta_graph_and_variables(
                sess, [tf.saved_model.tag_constants.SERVING],
                signature_def_map={
                    'predict_images':
                    prediction_signature,
                    tf.saved_model.signature_constants.
                    DEFAULT_SERVING_SIGNATURE_DEF_KEY:
                    classification_signature,
                },
                legacy_init_op=legacy_init_op)
            
            builder.save()
            print('Successfully exported model to %s' % FLAGS.output_dir)

if __name__ == '__main__':
    tf.app.run()
