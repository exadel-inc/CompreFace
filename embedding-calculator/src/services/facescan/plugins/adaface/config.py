import argparse
import sys
import os
from time import gmtime, strftime

def get_args():

    parent_parser = argparse.ArgumentParser(add_help=False)
    parent_parser.add_argument('--data_root', type=str, default='')
    parent_parser.add_argument('--train_data_path', type=str, default='faces_emore/imgs')
    parent_parser.add_argument('--val_data_path', type=str, default='faces_emore')
    parent_parser.add_argument('--use_mxrecord', action='store_true')
    parent_parser.add_argument('--train_data_subset', action='store_true')
    parent_parser.add_argument('--swap_color_channel', action='store_true')
    parent_parser.add_argument('--prefix', type=str, default='default')
    parent_parser.add_argument('--gpus', type=int, default=1, help='how many gpus')
    parent_parser.add_argument('--distributed_backend', type=str, default='ddp', choices=('dp', 'ddp', 'ddp2'),)
    parent_parser.add_argument('--use_16bit', action='store_true', help='if true uses 16 bit precision')
    parent_parser.add_argument('--epochs', default=24, type=int, metavar='N', help='number of total epochs to run')
    parent_parser.add_argument('--seed', type=int, default=42, help='seed for initializing training.')
    parent_parser.add_argument('--batch_size', default=256, type=int,
                               help='mini-batch size (default: 256), this is the total '
                                    'batch size of all GPUs on the current node when '
                                    'using Data Parallel or Distributed Data Parallel')

    parent_parser.add_argument('--lr',help='learning rate',default=1e-1, type=float)
    parent_parser.add_argument('--lr_milestones', default='8,12,14', type=str, help='epochs for reducing LR')
    parent_parser.add_argument('--lr_gamma', default=0.1, type=float, help='multiply when reducing LR')

    parent_parser.add_argument('--num_workers', default=16, type=int)
    parent_parser.add_argument('--fast_dev_run', dest='fast_dev_run', action='store_true')
    parent_parser.add_argument('--evaluate', action='store_true', help='use with start_from_model_statedict')
    parent_parser.add_argument('--resume_from_checkpoint', type=str, default='')
    parent_parser.add_argument('--start_from_model_statedict', type=str, default='')
    parent_parser.add_argument('--use_wandb', action='store_true')
    parent_parser.add_argument('--custom_num_class', type=int, default=-1)

    parser = add_task_arguments(parent_parser)
    args = parser.parse_args()

    args.lr_milestones = [int(x) for x in args.lr_milestones.split(',')]

    # set working dir
    current_time = strftime("%m-%d_0", gmtime())
    args.output_dir = os.path.join('experiments', args.prefix + "_" + current_time)
    if os.path.isdir(args.output_dir):
        while True:
            cur_exp_number = int(args.output_dir[-2:].replace('_', ""))
            args.output_dir = args.output_dir[:-2] + "_{}".format(cur_exp_number+1)
            if not os.path.isdir(args.output_dir):
                break
    
    return args


def add_task_arguments(parser):
    parser.add_argument('--arch', default='ir_18')
    parser.add_argument('--momentum', default=0.9, type=float, metavar='M')
    parser.add_argument('--weight_decay', default=1e-4, type=float)

    parser.add_argument('--head', default='adaface', type=str)
    parser.add_argument('--m', default=0.5, type=float)
    parser.add_argument('--h', default=0.0, type=float)
    parser.add_argument('--s', type=float, default=64.0)
    parser.add_argument('--t_alpha', default=0.01, type=float)

    parser.add_argument('--low_res_augmentation_prob', default=0.0, type=float)
    parser.add_argument('--crop_augmentation_prob', default=0.0, type=float)
    parser.add_argument('--photometric_augmentation_prob', default=0.0, type=float)

    parser.add_argument('--accumulate_grad_batches', type=int, default=1)
    parser.add_argument('--test_run', action='store_true')
    parser.add_argument('--save_all_models', action='store_true')
    return parser