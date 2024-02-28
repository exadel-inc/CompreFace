
from PFE.ijbs import IJBSTest
import PFE.utils
import numpy as np
from tqdm import tqdm
import math
from functools import partial
import os


def write_result(write_path, title, values=None):
    with open(write_path, 'a') as f:
        if values is None:
            f.write('{}\n'.format(title))
        else:
            f.write('{},{}\n'.format(title, ",".join([str(v) for v in values])))

def eval_IJBS(feat_func,
              image_paths,
              fuse_match_method='mean_cos',
              subsample=16,
              verbose=True,
              get_retrievals=False,
              save_root='./',
              ijbs_proto_path=None):


    if fuse_match_method == 'mean_cos':
        fuse_func = PFE.utils.average_fuse
        compare_func = PFE.utils.inner_product
    elif fuse_match_method == 'PFE_fuse':
        fuse_func = partial(PFE.utils.aggregate_PFE_v1, normalize=True, concatenate=False, return_sigma=False)
        compare_func = PFE.utils.inner_product
    elif fuse_match_method == 'PFE_fuse_match':
        fuse_func = partial(PFE.utils.aggregate_PFE_v1, normalize=True, concatenate=True, return_sigma=True)
        compare_func = PFE.utils.uncertain_score_simple
    elif fuse_match_method == 'pre_norm_vector_add_cos':
        # it is same as averaging. But the features were multiplied by norm beforehand
        fuse_func = PFE.utils.average_fuse
        compare_func = PFE.utils.inner_product
    elif fuse_match_method == 'norm_weighted_avg':
        raise ValueError('cannot implement')
    else:
        raise ValueError('not a corect fuse_match_method {}'.format(fuse_match_method))


    # ijbs_dataset_path = os.environ['DATABASES2'] + '/FaceDatabases/IJB-S/list_ijbs_mtcnncaffe_aligned.txt'
    if ijbs_proto_path is None:
        ijbs_proto_path = os.path.join(os.path.dirname(__file__), 'IJB_S_proto.pkl')

    tester = IJBSTest()
    tester.load(ijbs_proto_path)
    tester.initialize_indices(image_paths)

    has_indice = [1 for template in tester.all_template_list if template.indices is not None]
    len(has_indice)

    # tester.image_paths = np.array([str(p).replace('/media/shiyichu/Data', '/scratch/shiyichu/dataset') for p in tester.image_paths], dtype=np.object)
    tester.compare_func = compare_func
    for i, template in tqdm(enumerate(tester.all_template_list)):
        if template.indices is not None:
            if type(feat_func) is np.ndarray:
                features = feat_func[template.indices]
            elif hasattr(template, 'images'):
                features = feat_func(template.images)
            else:
                indices = template.indices
                if subsample:
                    chunk = int(math.ceil(len(indices) / subsample))
                    sub_indices = np.unique(np.arange(len(indices)) // chunk) * chunk
                    indices = indices[sub_indices]
                features = feat_func(tester.image_paths[indices])
            
            # fuse
            template.feature = fuse_func(features)
        else:
            if fuse_match_method == 'mean_cos':
                template.feature = np.zeros(512)
            elif fuse_match_method == 'PFE_fuse':
                template.feature = np.zeros(512)
            elif fuse_match_method == 'PFE_fuse_match':
                template.feature = np.stack([np.zeros(512), np.ones(512)], axis=-1)
            elif fuse_match_method == 'pre_norm_vector_add_cos':
                template.feature = np.zeros(512)
            elif fuse_match_method == 'norm_weighted_avg':
                raise ValueError('cannot implement')
            else:
                raise ValueError('not a correct fuse method')
    all_result = {}

    print('surveillance to single')
    if get_retrievals:
        result = tester.surveillance_to_single(get_retrievals=True)
        np.save(os.path.join(save_root, 'eval_result/{}_sur_to_sin.npy'.format(fuse_match_method)), result)
    DIRs_closeset, DIRs_openset = tester.surveillance_to_single()
    if save_root is not None:
        write_result(os.path.join(save_root, 'eval_result/{}_result.csv'.format(fuse_match_method)), title='surveillance_to_single', values=['rank1', 'rank5', 'rank10'])
        write_result(os.path.join(save_root, 'eval_result/{}_result.csv'.format(fuse_match_method)), title='closedset', values=DIRs_closeset.tolist())
        write_result(os.path.join(save_root, 'eval_result/{}_result.csv'.format(fuse_match_method)), title='surveillance_to_single', values=['0.01_FPIR', '0.1_FPIR'])
        write_result(os.path.join(save_root, 'eval_result/{}_result.csv'.format(fuse_match_method)), title='openset', values=DIRs_openset.tolist())
    print(DIRs_closeset)
    print(DIRs_openset)
    all_result['closed_{}'.format('surveillance_to_single')] = DIRs_closeset
    all_result['open_{}'.format('surveillance_to_single')] = DIRs_openset


    print('surveillance_to_booking')
    if get_retrievals:
        result = tester.surveillance_to_booking(get_retrievals=True)
        np.save(os.path.join(save_root, 'eval_result/{}_sur_to_book.npy'.format(fuse_match_method)), result)
    DIRs_closeset, DIRs_openset = tester.surveillance_to_booking()
    if save_root is not None:
        write_result(os.path.join(save_root, 'eval_result/{}_result.csv'.format(fuse_match_method)), title='surveillance_to_booking', values=['rank1', 'rank5', 'rank10'])
        write_result(os.path.join(save_root, 'eval_result/{}_result.csv'.format(fuse_match_method)), title='closedset', values=DIRs_closeset.tolist())
        write_result(os.path.join(save_root, 'eval_result/{}_result.csv'.format(fuse_match_method)), title='surveillance_to_booking', values=['0.01_FPIR', '0.1_FPIR'])
        write_result(os.path.join(save_root, 'eval_result/{}_result.csv'.format(fuse_match_method)), title='openset', values=DIRs_openset.tolist())
    print(DIRs_closeset)
    print(DIRs_openset)
    all_result['closed_{}'.format('surveillance_to_booking')] = DIRs_closeset
    all_result['open_{}'.format('surveillance_to_booking')] = DIRs_openset

    print('multiview_surveillance_to_booking')
    if get_retrievals:
        result = tester.multiview_surveillance_to_booking(get_retrievals=True)
        np.save(os.path.join(save_root, 'eval_result/{}_multi_to_book.npy'.format(fuse_match_method)), result)
    DIRs_closeset, DIRs_openset = tester.multiview_surveillance_to_booking()
    if save_root is not None:
        write_result(os.path.join(save_root, 'eval_result/{}_result.csv'.format(fuse_match_method)), title='multiview_surveillance_to_booking', values=['rank1', 'rank5', 'rank10'])
        write_result(os.path.join(save_root, 'eval_result/{}_result.csv'.format(fuse_match_method)), title='closedset', values=DIRs_closeset.tolist())
        write_result(os.path.join(save_root, 'eval_result/{}_result.csv'.format(fuse_match_method)), title='multiview_surveillance_to_booking', values=['0.01_FPIR', '0.1_FPIR'])
        write_result(os.path.join(save_root, 'eval_result/{}_result.csv'.format(fuse_match_method)), title='openset', values=DIRs_openset.tolist())
    print(DIRs_closeset)
    print(DIRs_openset)
    all_result['closed_{}'.format('multiview_surveillance_to_booking')] = DIRs_closeset
    all_result['open_{}'.format('multiview_surveillance_to_booking')] = DIRs_openset

    print('surveillance_to_surveillance')
    if get_retrievals:
        result = tester.surveillance_to_surveillance(get_retrievals=True)
        np.save(os.path.join(save_root, 'eval_result/{}_sur_to_sur.npy'.format(fuse_match_method)), result)
    DIRs_closeset, DIRs_openset = tester.surveillance_to_surveillance()
    if save_root is not None:
        write_result(os.path.join(save_root, 'eval_result/{}_result.csv'.format(fuse_match_method)), title='surveillance_to_surveillance', values=['rank1', 'rank5', 'rank10'])
        write_result(os.path.join(save_root, 'eval_result/{}_result.csv'.format(fuse_match_method)), title='closedset', values=DIRs_closeset.tolist())
        write_result(os.path.join(save_root, 'eval_result/{}_result.csv'.format(fuse_match_method)), title='surveillance_to_surveillance', values=['0.01_FPIR', '0.1_FPIR'])
        write_result(os.path.join(save_root, 'eval_result/{}_result.csv'.format(fuse_match_method)), title='openset', values=DIRs_openset.tolist())
    print(DIRs_closeset)
    print(DIRs_openset)
    all_result['closed_{}'.format('surveillance_to_surveillance')] = DIRs_closeset
    all_result['open_{}'.format('surveillance_to_surveillance')] = DIRs_openset

    print('uav_surveillance_to_booking')
    if get_retrievals:
        result = tester.uav_surveillance_to_booking(get_retrievals=True)
        np.save(os.path.join(save_root, 'eval_result/{}_uav_to_sur.npy'.format(fuse_match_method)), result)
    DIRs_closeset, DIRs_openset = tester.uav_surveillance_to_booking()
    if save_root is not None:
        write_result(os.path.join(save_root, 'eval_result/{}_result.csv'.format(fuse_match_method)), title='uav_surveillance_to_booking', values=['rank1', 'rank5', 'rank10'])
        write_result(os.path.join(save_root, 'eval_result/{}_result.csv'.format(fuse_match_method)), title='closedset', values=DIRs_closeset.tolist())
        write_result(os.path.join(save_root, 'eval_result/{}_result.csv'.format(fuse_match_method)), title='uav_surveillance_to_booking', values=['0.01_FPIR', '0.1_FPIR'])
        write_result(os.path.join(save_root, 'eval_result/{}_result.csv'.format(fuse_match_method)), title='openset', values=DIRs_openset.tolist())
    print(DIRs_closeset)
    print(DIRs_openset)
    all_result['closed_{}'.format('uav_surveillance_to_booking')] = DIRs_closeset
    all_result['open_{}'.format('uav_surveillance_to_booking')] = DIRs_openset

    return all_result


def run_eval_with_features(save_root, features, image_paths, get_retrievals=False, fuse_match_method='mean_cos', ijbs_proto_path=None):
    assert len(features) == len(image_paths)

    all_result = eval_IJBS(feat_func=features,
              fuse_match_method=fuse_match_method,
              image_paths=image_paths,
              subsample=False,
              verbose=True,
              get_retrievals=get_retrievals,
              save_root=save_root,
              ijbs_proto_path=ijbs_proto_path
              )
    return all_result
