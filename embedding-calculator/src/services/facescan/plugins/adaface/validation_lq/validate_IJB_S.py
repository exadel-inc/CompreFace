import torch
import numpy as np
from tqdm import tqdm
import data_utils
import argparse
import pandas as pd
import evaluate_helper
import sys, os
sys.path.insert(0, os.path.dirname(os.getcwd()))
import net


def str2bool(v):
    return v.lower() in ("true", "t", "1")

def load_pretrained_model(model_name='ir50'):
    # load model and pretrained statedict
    ckpt_path = adaface_models[model_name][0]
    arch = adaface_models[model_name][1]

    model = net.build_model(arch)
    statedict = torch.load(ckpt_path)['state_dict']
    model_statedict = {key[6:]:val for key, val in statedict.items() if key.startswith('model.')}
    model.load_state_dict(model_statedict)
    model.eval()
    return model

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='')
    parser.add_argument("--data_root", type=str, default='/data/data/faces/IJB/IJB_S')
    parser.add_argument('--model_name', type=str, default='ir50')
    parser.add_argument('--batch_size', type=int, default=512)
    parser.add_argument('--gpu', default=0, type=int, help='gpu id')
    parser.add_argument('--fuse_match_method', type=str, default='pre_norm_vector_add_cos',
                        choices=('pre_norm_vector_add_cos'))
    parser.add_argument('--save_features', action='store_true')

    args = parser.parse_args()

    adaface_models = {
        'ir50': ["../pretrained/adaface_ir50_ms1mv2.ckpt", 'ir_50'],
        'ir101_ms1mv2': ["../pretrained/adaface_ir101_ms1mv2.ckpt", 'ir_101'],
        'ir101_ms1mv3': ["../pretrained/adaface_ir101_ms1mv3.ckpt", 'ir_101'],
        'ir101_webface4m': ["../pretrained/adaface_ir101_webface4m.ckpt", 'ir_101'],
        'ir101_webface12m': ["../pretrained/adaface_ir101_webface12m.ckpt", 'ir_101'],
    }
    assert args.model_name in adaface_models

    # load model
    model = load_pretrained_model(args.model_name)
    model.to('cuda:{}'.format(args.gpu))

    # make result save root
    save_root = './result/IJBS/{}'.format(args.model_name)
    os.makedirs(save_root, exist_ok=True)
    image_path_df = pd.read_csv('./image_list_mtcnn_fail_skipped_v1.csv', index_col=0)
    all_image_paths = image_path_df['path'].apply(lambda x:os.path.join(args.data_root, x)).tolist()

    num_partition = 100
    dataset_split = np.array_split(all_image_paths, num_partition)

    print('total {} images'.format(len(all_image_paths)))
    all_features = []
    for partition_idx in tqdm(range(num_partition)):

        image_paths = list(dataset_split[partition_idx])
        dataloader = data_utils.prepare_imagelist_dataloader(image_paths, batch_size=args.batch_size, num_workers=8)

        size = len(dataloader.dataset)
        num_batches = len(dataloader)
        model.eval()

        features = []
        norms = []
        prev_max_idx = 0
        with torch.no_grad():
            for iter_idx, (img, idx) in enumerate(dataloader):
                assert idx.max().item() > prev_max_idx
                prev_max_idx = idx.max().item()  # order shifting by dataloader checking
                if iter_idx % 100 == 0:
                    print(f"{iter_idx} / {len(dataloader)} done")
                feature = model(img.to("cuda:0"))

                if isinstance(feature, tuple) and len(feature) == 2:
                    feature, norm = feature
                    features.append(feature.cpu().numpy())
                    norms.append(norm.cpu().numpy())
                else:
                    features.append(feature.cpu().numpy())

        features = np.concatenate(features, axis=0)
        if args.save_features:
            save_path = os.path.join(save_root, 'feature_extracted/ijbs_pred_{}_{}.npy'.format(args.model_name, partition_idx))
            os.makedirs(os.path.dirname(save_path), exist_ok=True)
            np.save(save_path, features)

        if len(norms) > 0:
            norms = np.concatenate(norms, axis=0)
            if args.save_features:
                save_path = os.path.join(save_root, 'feature_extracted/ijbs_pred_{}_norm_{}.npy'.format(args.model_name, partition_idx))
                np.save(save_path, norms)

        if args.fuse_match_method == 'pre_norm_vector_add_cos':
            features = features * norms
        all_features.append(features)
    all_features = np.concatenate(all_features, axis=0)

    # prepare savedir
    os.makedirs(os.path.join(save_root, 'eval_result'), exist_ok=True)
    # evaluate
    evaluate_helper.run_eval_with_features(save_root=save_root,
                                    features=all_features,
                                    image_paths=all_image_paths,
                                    get_retrievals=True,
                                    fuse_match_method=args.fuse_match_method)
