"""Utilities for training and testing
"""
# MIT License
# 
# Copyright (c) 2017 Yichun Shi
# 
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
# 
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
# 
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

import sys
import os
import numpy as np
from scipy import misc
import imp
import time
import math
import random
from datetime import datetime
import shutil

def import_file(full_path_to_module, name='module.name'):
    
    module_obj = imp.load_source(name, full_path_to_module)
    
    return module_obj

def create_log_dir(log_base_dir, name, config_file, model_file):
    subdir = datetime.strftime(datetime.now(), '%Y%m%d-%H%M%S')
    log_dir = os.path.join(os.path.expanduser(log_base_dir), name, subdir)
    if not os.path.isdir(log_dir):  # Create the log directory if it doesn't exist
        os.makedirs(log_dir)
    shutil.copyfile(config_file, os.path.join(log_dir,'config.py'))
    shutil.copyfile(model_file, os.path.join(log_dir,'model.py'))

    return log_dir

def get_updated_learning_rate(global_step, learning_rate_strategy, learning_rate_schedule):
    if learning_rate_strategy == 'step':
        max_step = -1
        learning_rate = 0.0
        for step, lr in learning_rate_schedule.items():
            if global_step >= step and step > max_step:
                learning_rate = lr
                max_step = step
        if max_step == -1:
            raise ValueError('cannot find learning rate for step %d' % global_step)
    elif learning_rate_strategy == 'cosine':
        initial = learning_rate_schedule['initial']
        interval = learning_rate_schedule['interval']
        end_step = learning_rate_schedule['end_step']
        step = math.floor(float(global_step) / interval) * interval
        assert step <= end_step
        learning_rate = initial * 0.5 * (math.cos(math.pi * step / end_step) + 1)
    return learning_rate

def display_info(epoch, step, watch_list):
    sys.stdout.write('[%d][%d]' % (epoch+1, step+1))
    for item in watch_list.items():
        if type(item[1]) in [float, np.float32, np.float64]:
            sys.stdout.write('   %s: %2.3f' % (item[0], item[1]))
        elif type(item[1]) in [int, bool, np.int32, np.int64, np.bool]:
            sys.stdout.write('   %s: %d' % (item[0], item[1]))
    sys.stdout.write('\n')

def get_pairwise_score_label(score_mat, label):
    n = label.size
    assert score_mat.shape[0]==score_mat.shape[1]==n
    triu_indices = np.triu_indices(n, 1)
    if len(label.shape)==1:
        label = label[:, None]
    label_mat = label==label.T
    score_vec = score_mat[triu_indices]
    label_vec = label_mat[triu_indices]
    return score_vec, label_vec


##########################
# Comparison Functions
##########################

def l2_normalize(x, axis=1, eps=1e-8):
    return x / (np.linalg.norm(x, axis=axis, keepdims=True) + eps)

def group_normalize(x, ngroup=1):
    N, C = x.shape
    assert C % ngroup == 0
    x = x.reshape(N, ngroup, C//ngroup)
    x = l2_normalize(x, axis=2) / math.sqrt(ngroup)
    x = x.reshape(N, C)
    return x

def pair_euc_score(x1, x2):
    x1, x2 = np.array(x1), np.array(x2)
    if x1.ndim == 3:
        x1, x2 = x1[:,:,0], x2[:,:,0]
    dist = np.sum(np.square(x1 - x2), axis=1)
    return -dist

def pair_cosine_score(x1, x2):
    x1, x2 = np.array(x1), np.array(x2)
    if x1.ndim == 3:
        x1, x2 = x1[:,:,0], x2[:,:,0]
    return np.sum(l2_normalize(x1) * l2_normalize(x2), axis=1)

def pair_inner_product(x1, x2):
    x1, x2 = np.array(x1), np.array(x2)
    if x1.ndim == 3:
        x1, x2 = x1[:,:,0], x2[:,:,0]
    return np.sum(x1*x2, axis=1)

def pair_hammin_distance(x1, x2):
    x1, x2 = np.array(x1), np.array(x2)
    x1, x2 = x1>=0, x2>=0
    return (x1==x2).sum(1)

def inner_product(x1, x2):
    x1, x2 = np.array(x1), np.array(x2)
    if x1.ndim == 3:
        x1, x2 = x1[:,:,0], x2[:,:,0]
    return np.dot(x1, x2.T)

def cosine_score(x1, x2):
    x1, x2 = np.array(x1), np.array(x2)
    if x1.ndim == 3:
        x1, x2 = x1[:,:,0], x2[:,:,0]
    x1 = l2_normalize(x1)
    x2 = l2_normalize(x2)
    return np.dot(x1, x2.T)

def euclidean(x1,x2):
    assert x1.shape[1]==x2.shape[1]
    x2 = x2.transpose()
    x1_norm = np.sum(np.square(x1), axis=1, keepdims=True)
    x2_norm = np.sum(np.square(x2), axis=0, keepdims=True)
    dist = x1_norm + x2_norm - 2*np.dot(x1,x2)
    return dist

def pair_uncertain_score(x1, x2, sigma_sq1=None, sigma_sq2=None):
    if sigma_sq1 is None:
        assert sigma_sq2 is None, 'either pass in concated features, or mu, sigma_sq for both!'
        x1, x2 = np.array(x1), np.array(x2)
        D = int(x1.shape[1] / 2)
        mu1, sigma_sq1 = x1[:,:,0], x1[:,:,1]
        mu2, sigma_sq2 = x2[:,:,0], x2[:,:,1]
    else:
        x1, x2 = np.array(x1), np.array(x2)
        sigma_sq1, sigma_sq2 = np.array(sigma_sq1), np.array(sigma_sq2)
        mu1, mu2 = x1, x2
    sigma_sq_mutual = sigma_sq1 + sigma_sq2
    dist = np.sum(np.square(mu1 - mu2) / sigma_sq_mutual + np.log(sigma_sq_mutual), axis=1)
    return -dist


def uncertain_score(x1, x2, sigma_sq1=None, sigma_sq2=None):
    if sigma_sq1 is None:
        assert sigma_sq2 is None, 'either pass in concated features, or mu, sigma_sq for both!'
        x1, x2 = np.array(x1), np.array(x2)
        D = int(x1.shape[1] / 2)
        mu1, sigma_sq1 = x1[:,:,0], x1[:,:,1]
        mu2, sigma_sq2 = x2[:,:,0], x2[:,:,1]
    else:
        x1, x2 = np.array(x1), np.array(x2)
        sigma_sq1, sigma_sq2 = np.array(sigma_sq1), np.array(sigma_sq2)
        mu1, mu2 = x1, x2
    from clib import mutual_likelihood_score_parallel
    mu1, mu2 = mu1.astype(np.float32), mu2.astype(np.float32)
    sigma_sq1, sigma_sq2 = sigma_sq1.astype(np.float32), sigma_sq2.astype(np.float32)
    score = mutual_likelihood_score_parallel(mu1, mu2, sigma_sq1, sigma_sq2)
    score = np.array(score)
    return score


def uncertain_score_simple(x1, x2, sigma_sq1=None, sigma_sq2=None):
    if sigma_sq1 is None:
        assert sigma_sq2 is None, 'either pass in concated features, or mu, sigma_sq for both!'
        x1, x2 = np.array(x1), np.array(x2)
        D = int(x1.shape[1] / 2)
        mu1, sigma_sq1 = x1[:,:,0], x1[:,:,1]
        mu2, sigma_sq2 = x2[:,:,0], x2[:,:,1]
    else:
        x1, x2 = np.array(x1), np.array(x2)
        sigma_sq1, sigma_sq2 = np.array(sigma_sq1), np.array(sigma_sq2)
        mu1, mu2 = x1, x2
    D = sigma_sq1.shape[1]
    sigma_sq1 = sigma_sq1.mean(1, keepdims=True)
    sigma_sq2 = sigma_sq2.mean(1, keepdims=True).T
    dist = euclidean(mu1,mu2)
    score = dist / (sigma_sq1+sigma_sq2) + D * np.log(sigma_sq1+sigma_sq2)
    score = -np.array(score)
    return score

##########################
# Fusion Functions
##########################

def average_fuse(x):
    x = x.mean(0)
    x = l2_normalize(x, axis=0)
    return x

def aggregate_PFE(x, sigma_sq=None, normalize=True, concatenate=True):
    if sigma_sq is None:
        mu, sigma_sq = x[:,:,0], x[:,:,1]
    else:
        mu = x
    attention = 1. / sigma_sq
    attention = attention / np.sum(attention, axis=0, keepdims=True)
    
    mu_new  = np.sum(mu * attention, axis=0)
    sigma_sq_new = np.min(sigma_sq, axis=0)

    if normalize:
        ngroup = 1
        mu_new = mu_new.reshape(-1, 512)
        # mu_new = l2_normalize(mu_new, axis=-1)
        mu_new = group_normalize(mu_new, ngroup)
        mu_new = mu_new.reshape(-1)

    if concatenate:
        return np.stack([mu_new, sigma_sq_new], axis=1)
    else:
        return mu_new, sigma_sq_new

def l2_normalize_v1(x, axis=None, eps=1e-8):
    # from PFE github repo's ijbA eval code
    x = x / (eps + np.linalg.norm(x, axis=axis))
    return x

def aggregate_PFE_v1(x, sigma_sq=None, normalize=True, concatenate=False, return_sigma=True):
    # from PFE github repo's ijbA eval code
    if sigma_sq is None:
        D = int(x.shape[1] / 2)
        mu, sigma_sq = x[:,:D], x[:,D:]
    else:
        mu = x
    attention = 1. / sigma_sq
    attention = attention / np.sum(attention, axis=0, keepdims=True)
    
    mu_new  = np.sum(mu * attention, axis=0)
    sigma_sq_new = np.min(sigma_sq, axis=0)

    if normalize:
        mu_new = l2_normalize_v1(mu_new)
    
    if not return_sigma:
        return mu_new

    if concatenate:
        return np.stack([mu_new, sigma_sq_new], axis=-1)
    else:
        return mu_new, sigma_sq_new
    

def write_summary(summary_writer, summary, global_step):
    if 'scalar' in summary:
        for k,v in summary['scalar'].items():
            summary_writer.add_scalar(k, v, global_step)
    if 'histogram' in summary:
        for k,v in summary['histogram'].items():
            summary_writer.add_histogram(k, v, global_step)
    if 'image' in summary:
        for k,v in summary['image'].items():
            summary_writer.add_image(k, v, global_step)
    if 'figure' in summary:
        for k,v in summary['figure'].items():
            summary_writer.add_figure(k, v, global_step)
    summary_writer.file_writer.flush()
