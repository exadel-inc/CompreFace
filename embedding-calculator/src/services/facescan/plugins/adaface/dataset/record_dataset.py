import numbers
import mxnet as mx
import os
from torch.utils.data import Dataset
import numpy as np
import torch
from PIL import Image
import pandas as pd
import cv2
from .augmenter import Augmenter


class BaseMXDataset(Dataset):
    def __init__(self, root_dir, swap_color_channel=False):
        super(BaseMXDataset, self).__init__()
        self.root_dir = root_dir
        path_imgrec = os.path.join(root_dir, 'train.rec')
        path_imgidx = os.path.join(root_dir, 'train.idx')
        path_imglst = os.path.join(root_dir, 'train.lst')

        self.record = mx.recordio.MXIndexedRecordIO(path_imgidx, path_imgrec, 'r')

        # grad image index from the record and know how many images there are.
        # image index could be occasionally random order. like [4,3,1,2,0]
        s = self.record.read_idx(0)
        header, _ = mx.recordio.unpack(s)
        if header.flag > 0:
            self.header0 = (int(header.label[0]), int(header.label[1]))
            self.imgidx = np.array(range(1, int(header.label[0])))
        else:
            self.imgidx = np.array(list(self.record.keys))
        print('record file length', len(self.imgidx))

        record_info = []
        for idx in self.imgidx:
            s = self.record.read_idx(idx)
            header, _ = mx.recordio.unpack(s)
            label = header.label
            row = {'idx': idx, 'path': '{}/name.jpg'.format(label), 'label': label}
            record_info.append(row)
        self.record_info = pd.DataFrame(record_info)

        self.swap_color_channel = swap_color_channel
        if self.swap_color_channel:
            print('[INFO] Train data in swap_color_channel')

    def read_sample(self, index):
        idx = self.imgidx[index]
        s = self.record.read_idx(idx)
        header, img = mx.recordio.unpack(s)
        label = header.label
        if not isinstance(label, numbers.Number):
            label = label[0]
        label = torch.tensor(label, dtype=torch.long)
        sample = mx.image.imdecode(img).asnumpy()
        sample = Image.fromarray(np.asarray(sample)[:, :, ::-1])

        if self.swap_color_channel:
            # swap RGB to BGR if sample is in RGB
            # we need sample in BGR
            sample = Image.fromarray(np.asarray(sample)[:, :, ::-1])
        return sample, label

    def __getitem__(self, index):
        raise NotImplementedError()

    def __len__(self):
        return len(self.imgidx)



class AugmentRecordDataset(BaseMXDataset):
    def __init__(self,
                 root_dir,
                 transform=None,
                 low_res_augmentation_prob=0.0,
                 crop_augmentation_prob=0.0,
                 photometric_augmentation_prob=0.0,
                 swap_color_channel=False,
                 output_dir='./'
                 ):
        super(AugmentRecordDataset, self).__init__(root_dir,
                                                   swap_color_channel=swap_color_channel,
                                                   )
        self.augmenter = Augmenter(crop_augmentation_prob, photometric_augmentation_prob, low_res_augmentation_prob)
        self.transform = transform
        self.output_dir = output_dir

    def __getitem__(self, index):
        sample, target = self.read_sample(index)

        sample = self.augmenter.augment(sample)
        sample_save_path = os.path.join(self.output_dir, 'training_samples', 'sample.jpg')
        if not os.path.isfile(sample_save_path):
            os.makedirs(os.path.dirname(sample_save_path), exist_ok=True)
            cv2.imwrite(sample_save_path, np.array(sample))  # the result has to look okay (Not color swapped)

        if self.transform is not None:
            sample = self.transform(sample)

        return sample, target
