from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

from six.moves import xrange
from datetime import datetime
import time
import os
import numpy as np
import tensorflow as tf
from .data import distorted_inputs
from .model import select_model
import json
import re


LAMBDA = 0.01
MOM = 0.9
tf.app.flags.DEFINE_string('pre_checkpoint_path', '',
                           """If specified, restore this pretrained model """
                           """before beginning any training.""")

tf.app.flags.DEFINE_string('train_dir', '/home/dpressel/dev/work/AgeGenderDeepLearning/Folds/tf/test_fold_is_0',
                           'Training directory')

tf.app.flags.DEFINE_boolean('log_device_placement', False,
                            """Whether to log device placement.""")

tf.app.flags.DEFINE_integer('num_preprocess_threads', 4,
                            'Number of preprocessing threads')

tf.app.flags.DEFINE_string('optim', 'Momentum',
                           'Optimizer')

tf.app.flags.DEFINE_integer('image_size', 227,
                            'Image size')

tf.app.flags.DEFINE_float('eta', 0.01,
                          'Learning rate')

tf.app.flags.DEFINE_float('pdrop', 0.,
                          'Dropout probability')

tf.app.flags.DEFINE_integer('max_steps', 40000,
                          'Number of iterations')

tf.app.flags.DEFINE_integer('steps_per_decay', 10000,
                            'Number of steps before learning rate decay')
tf.app.flags.DEFINE_float('eta_decay_rate', 0.1,
                          'Learning rate decay')

tf.app.flags.DEFINE_integer('epochs', -1,
                            'Number of epochs')

tf.app.flags.DEFINE_integer('batch_size', 128,
                            'Batch size')

tf.app.flags.DEFINE_string('checkpoint', 'checkpoint',
                          'Checkpoint name')

tf.app.flags.DEFINE_string('model_type', 'default',
                           'Type of convnet')

tf.app.flags.DEFINE_string('pre_model',
                            '',#'./inception_v3.ckpt',
                           'checkpoint file')
FLAGS = tf.app.flags.FLAGS

# Every 5k steps cut learning rate in half
def exponential_staircase_decay(at_step=10000, decay_rate=0.1):

    print('decay [%f] every [%d] steps' % (decay_rate, at_step))
    def _decay(lr, global_step):
        return tf.train.exponential_decay(lr, global_step,
                                          at_step, decay_rate, staircase=True)
    return _decay

def optimizer(optim, eta, loss_fn, at_step, decay_rate):
    global_step = tf.Variable(0, trainable=False)
    optz = optim
    if optim == 'Adadelta':
        optz = lambda lr: tf.train.AdadeltaOptimizer(lr, 0.95, 1e-6)
        lr_decay_fn = None
    elif optim == 'Momentum':
        optz = lambda lr: tf.train.MomentumOptimizer(lr, MOM)
        lr_decay_fn = exponential_staircase_decay(at_step, decay_rate)

    return tf.contrib.layers.optimize_loss(loss_fn, global_step, eta, optz, clip_gradients=4., learning_rate_decay_fn=lr_decay_fn)

def loss(logits, labels):
    labels = tf.cast(labels, tf.int32)
    cross_entropy = tf.nn.sparse_softmax_cross_entropy_with_logits(
        logits=logits, labels=labels, name='cross_entropy_per_example')
    cross_entropy_mean = tf.reduce_mean(cross_entropy, name='cross_entropy')
    tf.add_to_collection('losses', cross_entropy_mean)
    losses = tf.get_collection('losses')
    regularization_losses = tf.get_collection(tf.GraphKeys.REGULARIZATION_LOSSES)
    total_loss = cross_entropy_mean + LAMBDA * sum(regularization_losses)
    tf.summary.scalar('tl (raw)', total_loss)
    #total_loss = tf.add_n(losses + regularization_losses, name='total_loss')
    loss_averages = tf.train.ExponentialMovingAverage(0.9, name='avg')
    loss_averages_op = loss_averages.apply(losses + [total_loss])
    for l in losses + [total_loss]:
        tf.summary.scalar(l.op.name + ' (raw)', l)
        tf.summary.scalar(l.op.name, loss_averages.average(l))
    with tf.control_dependencies([loss_averages_op]):
        total_loss = tf.identity(total_loss)
    return total_loss

def main(argv=None):
    with tf.Graph().as_default():

        model_fn = select_model(FLAGS.model_type)
        # Open the metadata file and figure out nlabels, and size of epoch
        input_file = os.path.join(FLAGS.train_dir, 'md.json')
        print(input_file)
        with open(input_file, 'r') as f:
            md = json.load(f)

        images, labels, _ = distorted_inputs(FLAGS.train_dir, FLAGS.batch_size, FLAGS.image_size, FLAGS.num_preprocess_threads)
        logits = model_fn(md['nlabels'], images, 1-FLAGS.pdrop, True)
        total_loss = loss(logits, labels)

        train_op = optimizer(FLAGS.optim, FLAGS.eta, total_loss, FLAGS.steps_per_decay, FLAGS.eta_decay_rate)
        saver = tf.train.Saver(tf.global_variables())
        summary_op = tf.summary.merge_all()

        sess = tf.Session(config=tf.ConfigProto(
            log_device_placement=FLAGS.log_device_placement))

        tf.global_variables_initializer().run(session=sess)

        # This is total hackland, it only works to fine-tune iv3
        if FLAGS.pre_model:
            inception_variables = tf.get_collection(
                tf.GraphKeys.VARIABLES, scope="InceptionV3")
            restorer = tf.train.Saver(inception_variables)
            restorer.restore(sess, FLAGS.pre_model)

        if FLAGS.pre_checkpoint_path:
            if tf.gfile.Exists(FLAGS.pre_checkpoint_path) is True:
                print('Trying to restore checkpoint from %s' % FLAGS.pre_checkpoint_path)
                restorer = tf.train.Saver()
                tf.train.latest_checkpoint(FLAGS.pre_checkpoint_path)
                print('%s: Pre-trained model restored from %s' %
                      (datetime.now(), FLAGS.pre_checkpoint_path))


        run_dir = '%s/run-%d' % (FLAGS.train_dir, os.getpid())

        checkpoint_path = '%s/%s' % (run_dir, FLAGS.checkpoint)
        if tf.gfile.Exists(run_dir) is False:
            print('Creating %s' % run_dir)
            tf.gfile.MakeDirs(run_dir)

        tf.train.write_graph(sess.graph_def, run_dir, 'model.pb', as_text=True)

        tf.train.start_queue_runners(sess=sess)


        summary_writer = tf.summary.FileWriter(run_dir, sess.graph)
        steps_per_train_epoch = int(md['train_counts'] / FLAGS.batch_size)
        num_steps = FLAGS.max_steps if FLAGS.epochs < 1 else FLAGS.epochs * steps_per_train_epoch
        print('Requested number of steps [%d]' % num_steps)

        
        for step in xrange(num_steps):
            start_time = time.time()
            _, loss_value = sess.run([train_op, total_loss])
            duration = time.time() - start_time

            assert not np.isnan(loss_value), 'Model diverged with loss = NaN'

            if step % 10 == 0:
                num_examples_per_step = FLAGS.batch_size
                examples_per_sec = num_examples_per_step / duration
                sec_per_batch = float(duration)
                
                format_str = ('%s: step %d, loss = %.3f (%.1f examples/sec; %.3f ' 'sec/batch)')
                print(format_str % (datetime.now(), step, loss_value,
                                    examples_per_sec, sec_per_batch))

            # Loss only actually evaluated every 100 steps?
            if step % 100 == 0:
                summary_str = sess.run(summary_op)
                summary_writer.add_summary(summary_str, step)
                
            if step % 1000 == 0 or (step + 1) == num_steps:
                saver.save(sess, checkpoint_path, global_step=step)

if __name__ == '__main__':
    tf.app.run()
