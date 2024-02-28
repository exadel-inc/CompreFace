import os
import pickle
import torch
import torch.distributed as dist


class dotdict(dict):
    """dot.notation access to dictionary attributes"""
    __getattr__ = dict.get
    __setattr__ = dict.__setitem__
    __delattr__ = dict.__delitem__

def l2_norm(input, axis=1):
    """l2 normalize
    """
    norm = torch.norm(input, 2, axis, True)
    output = torch.div(input, norm)
    return output, norm

def fuse_features_with_norm(stacked_embeddings, stacked_norms):

    assert stacked_embeddings.ndim == 3 # (n_features_to_fuse, batch_size, channel)
    assert stacked_norms.ndim == 3 # (n_features_to_fuse, batch_size, 1)
    
    pre_norm_embeddings = stacked_embeddings * stacked_norms
    fused = pre_norm_embeddings.sum(dim=0)
    fused, fused_norm = l2_norm(fused, axis=1)

    return fused, fused_norm 


def is_dist_avail_and_initialized():
    if not dist.is_available():
        return False
    if not dist.is_initialized():
        return False
    return True

def get_world_size():
    if not is_dist_avail_and_initialized():
        return 1
    return dist.get_world_size()

def get_local_rank():
    if not is_dist_avail_and_initialized():
        return 0
    return int(os.environ["LOCAL_RANK"])

def all_gather(data):
    """
    Run all_gather on arbitrary picklable data (not necessarily tensors)
    Args:
        data: any picklable object
    Returns:
        list[data]: list of data gathered from each rank
    """
    world_size = get_world_size()
    local_rank = get_local_rank()
    if world_size == 1:
        return [data]

    # serialized to a Tensor
    buffer = pickle.dumps(data)
    storage = torch.ByteStorage.from_buffer(buffer)
    tensor = torch.ByteTensor(storage).to(local_rank)

    # obtain Tensor size of each rank
    local_size = torch.tensor([tensor.numel()], device=torch.device("cuda", local_rank))
    size_list = [torch.tensor([0], device=torch.device("cuda", local_rank)) for _ in range(world_size)]
    dist.all_gather(size_list, local_size)
    size_list = [int(size.item()) for size in size_list]
    max_size = max(size_list)

    # receiving Tensor from all ranks
    # we pad the tensor because torch all_gather does not support
    # gathering tensors of different shapes
    tensor_list = []
    for _ in size_list:
        tensor_list.append(torch.empty((max_size,), dtype=torch.uint8,
                            device=torch.device("cuda", local_rank)))
    if local_size != max_size:
        padding = torch.empty(size=(max_size - local_size,), dtype=torch.uint8,
                            device=torch.device("cuda", local_rank))
        tensor = torch.cat((tensor, padding), dim=0)
    dist.all_gather(tensor_list, tensor)

    data_list = []
    for size, tensor in zip(size_list, tensor_list):
        buffer = tensor.cpu().numpy().tobytes()[:size]
        data_list.append(pickle.loads(buffer))

    return data_list


def get_num_class(hparams):
    # getting number of subjects in the dataset
    if hparams.custom_num_class != -1:
        return hparams.custom_num_class

    if 'faces_emore' in hparams.train_data_path.lower():
        # MS1MV2
        class_num = 70722 if hparams.train_data_subset else 85742
    elif 'ms1m-retinaface-t1' in hparams.train_data_path.lower():
        # MS1MV3
        assert not hparams.train_data_subset
        class_num = 93431
    elif 'faces_vgg_112x112' in hparams.train_data_path.lower():
        # VGGFace2
        assert not hparams.train_data_subset
        class_num = 9131
    elif 'faces_webface_112x112' in hparams.train_data_path.lower():
        # CASIA-WebFace
        assert not hparams.train_data_subset
        class_num = 10572
    elif 'webface4m' in hparams.train_data_path.lower():
        assert not hparams.train_data_subset
        class_num = 205990
    elif 'webface12m' in hparams.train_data_path.lower():
        assert not hparams.train_data_subset
        class_num = 617970
    elif 'webface42m' in hparams.train_data_path.lower():
        assert not hparams.train_data_subset
        class_num = 2059906
    else:
        raise ValueError('Check your train_data_path', hparams.train_data_path)

    return class_num
