import os
import numpy as np
import scipy.io
import pickle
import multiprocessing

def list2array(feat):
    res = []
    dim = None
    for f in feat:
        if f is not None:
            f = f.reshape(-1)
            dim = f.shape[0]
            break
    for f in feat:
        if f is None:
            f = np.random.rand(dim)
        res.append(f)
    return np.array(res)

def euclidean(x1,x2):
    x1 = np.array(x1)
    x2 = np.array(x2)
    assert x1.shape[1]==x2.shape[1]
    x2 = x2.transpose()
    x1_norm = np.sum(np.square(x1), axis=1, keepdims=True)
    x2_norm = np.sum(np.square(x2), axis=0, keepdims=True)
    dist = x1_norm + x2_norm - 2*np.dot(x1,x2)
    return -1 * dist

def uncertain(x1,x2):
    x1 = np.array(x1)
    x2 = np.array(x2)
    mu1, sigma_sq1 = x1[:,:512], x1[:,512:]
    mu2, sigma_sq2 = x2[:,:512], x2[:,512:]
    dist =  utils.uncertain_dist(mu1, mu2, sigma_sq1, sigma_sq2)
    return -1 * dist

class IJBSTemplate:
    def __init__(self):
        self.subject_id = None # int
        self.videos = None # list
        self.images = None # list
        self.indices = None # numpy array
        self.feature = None # numpy array
        self.template_indices = None # indices of template for multi-view
        self.compare_func = uncertain

    def get_paths(self, image_paths):
        return image_paths[self.indices]

class IJBSTest:
    def __init__(self):
        self.all_template_list = []
        self.protocol_template_indices = {}
        self.image_paths = None


    def init_proto(self, folder_ijbs_protocols):

        filename = os.path.join(folder_ijbs_protocols, 'cs6_metadata.csv')
        with open(filename, 'r') as f:
            lines = f.readlines()
            meta_lines = lines[1:]

        ############ Get Video Templates ################
        print('Get Video Templates......')
        video_meta_list = [(x.split(',')[0], x.split(',')[1]) for x in meta_lines \
            if x.split(',')[1].startswith('videos')]
        video_meta_list = list(set(video_meta_list)) # list of (person_id, video_name) there are total 398 videos, but multiple people pop up in the video. so there are 3852 pairs

        subject_video_dict = {}
        for i,meta in enumerate(video_meta_list):
            template = IJBSTemplate()
            template.subject_id = int(meta[0])
            template.videos = [meta[1]]
            self.all_template_list.append(template)
            if template.subject_id not in subject_video_dict:
                subject_video_dict[template.subject_id] = [i]
            else:
                subject_video_dict[template.subject_id].append(i)
        ############ Get Video Templates ################

        ############ Get Video Gallery Templates ################ 
        print('Get Video Gallery Templates......')

        # getting 101 video list as 'video_gallery1' g1
        filename = os.path.join(folder_ijbs_protocols, 
            'galleries/cs6_surveillance_to_surveillance_g1.csv')
        with open(filename, 'r') as f:
            lines = f.readlines()
            lines = lines[1:]
        video_list = [(x.split(',')[1], x.split(',')[2]) for x in lines]
        video_list = list(set(video_list))
        self.protocol_template_indices['video_gallery1'] = [video_meta_list.index(x) \
            for x in video_list]
        self.protocol_template_indices['video_gallery1'] = np.array(
            self.protocol_template_indices['video_gallery1']).astype(
            int).flatten()

        # getting 101 video list as 'video_gallery2' g2
        filename = os.path.join(folder_ijbs_protocols, 
            'galleries/cs6_surveillance_to_surveillance_g2.csv')
        with open(filename, 'r') as f:
            lines = f.readlines()
            lines = lines[1:]
        video_list = [(x.split(',')[1], x.split(',')[2]) for x in lines]
        video_list = list(set(video_list))
        self.protocol_template_indices['video_gallery2'] = [video_meta_list.index(x) \
            for x in video_list]
        self.protocol_template_indices['video_gallery2'] = np.array(
            self.protocol_template_indices['video_gallery2']).astype(
            int).flatten()
        ############ Get Video Gallery Templates ################

        ############ Get Video to Image Probe Templates ################
        # you are running through list of (person_id, video_name) and appending if video_name is in 'cs6_surveillance_to_single-booking_probe.csv'
        print('Get Video to Image Probe Templates......')
        filename = os.path.join(folder_ijbs_protocols,
            'cs6_surveillance_to_single-booking_probe.csv')
        with open(filename, 'r') as f:
            lines = f.readlines()
            lines = lines[1:]
        video_list = [x.split('\n')[0] for x in lines]
        self.protocol_template_indices['video2image_probe'] = [x[0] for x in enumerate(
            video_meta_list) if x[1][1] in video_list]
        self.protocol_template_indices['video2image_probe'] = np.array(
            self.protocol_template_indices['video2image_probe']).astype(
            int).flatten()
        ############ Get Video to Image Probe Templates ################

        ############ Get Video to Video Probe Templates ################
        print('Get Video to Video Probe Templates......')
        filename = os.path.join(folder_ijbs_protocols,
            'cs6_surveillance_to_surveillance_probe.csv')
        with open(filename, 'r') as f:
            lines = f.readlines()
            lines = lines[1:]
        video_list = [x.split('\n')[0] for x in lines] # 376 length of video/4012.mp4

        # 1. Surveillance to Surveillance
        self.protocol_template_indices['video2video_probe'] = [x[0] for x in enumerate(
            video_meta_list) if x[1][1] in video_list]
        self.protocol_template_indices['video2video_probe'] = np.array(
            self.protocol_template_indices['video2video_probe']).astype(
            int).flatten()

        # 2. Multi-view Surveillance to booking
        self.protocol_template_indices['multiview_video_probe'] = []
        subject_list = [self.all_template_list[x].subject_id for x in \
            self.protocol_template_indices['video2video_probe']]
        subject_list = list(set(subject_list))
        index = len(video_meta_list)
        for subject in subject_list:
            template = IJBSTemplate()
            template.subject_id = subject
            template.videos = [video_meta_list[x][1] \
                for x in subject_video_dict[subject]]
            template.template_indices = np.array(
                subject_video_dict[subject]).astype(int).flatten()
            self.all_template_list.append(template)
            self.protocol_template_indices['multiview_video_probe'].append(index)
            index += 1
        self.protocol_template_indices['multiview_video_probe'] = np.array(
            self.protocol_template_indices['multiview_video_probe']).astype(
            int).flatten()
        ############ Get Video to Video Probe Templates ################

        ############ Get UAV Video Probe Templates ################
        print('Get UAV Video Probe Templates......')
        self.protocol_template_indices['uav_probe'] = []
        filename = os.path.join(folder_ijbs_protocols,
            'cs6_uav_to_single-booking_probe.csv')
        with open(filename, 'r') as f:
            lines = f.readlines()
            lines = lines[1:]
        uav_list = [x.split('\n')[0] for x in lines]
        index = len(self.all_template_list)
        for uav in uav_list:
            if uav.startswith('videos'):
                self.protocol_template_indices['uav_probe'].extend(
                    [x[0] for x in enumerate(video_meta_list) if x[1][1] == uav])
            else:
                template = IJBSTemplate()
                template.subject_id = [int(x.split(',')[0]) for x in meta_lines \
                    if x.split(',')[1] == uav][0]
                template.images = [uav]
                self.all_template_list.append(template)
                self.protocol_template_indices['uav_probe'].append(index)
                index += 1
        self.protocol_template_indices['uav_probe'] = np.array(
            self.protocol_template_indices['uav_probe']).astype(
            int).flatten()
        ############ Get UAV Video Probe Templates ################

        ############ Get Single Image Templates ################
        print('Get Single Image Templates......')
        index = len(self.all_template_list)

        self.protocol_template_indices['single_image_gallery1'] = []
        filename = os.path.join(folder_ijbs_protocols, 
            'galleries/cs6_surveillance_to_single_g1.csv')
        with open(filename, 'r') as f:
            lines = f.readlines()
            lines = lines[1:]
        image_list = [(x.split(',')[1], x.split(',')[2]) for x in lines]
        for image in image_list:
            template = IJBSTemplate()
            template.subject_id = int(image[0])
            template.images = [image[1]]
            self.all_template_list.append(template)
            self.protocol_template_indices['single_image_gallery1'].append(index)
            index += 1
        self.protocol_template_indices['single_image_gallery1'] = np.array(
            self.protocol_template_indices['single_image_gallery1']).astype(
            int).flatten()

        self.protocol_template_indices['single_image_gallery2'] = []
        filename = os.path.join(folder_ijbs_protocols, 
            'galleries/cs6_surveillance_to_single_g2.csv')
        with open(filename, 'r') as f:
            lines = f.readlines()
            lines = lines[1:]
        image_list = [(x.split(',')[1], x.split(',')[2]) for x in lines]
        for image in image_list:
            template = IJBSTemplate()
            template.subject_id = int(image[0])
            template.images = [image[1]]
            self.all_template_list.append(template)
            self.protocol_template_indices['single_image_gallery2'].append(index)
            index += 1
        self.protocol_template_indices['single_image_gallery2'] = np.array(
            self.protocol_template_indices['single_image_gallery2']).astype(
            int).flatten()
        ############ Get Single Image Templates ################

        ############ Get Booking Image Templates ################
        print('Get Booking Image Templates......')
        index = len(self.all_template_list)

        self.protocol_template_indices['booking_image_gallery1'] = []
        filename = os.path.join(folder_ijbs_protocols,
            'galleries/cs6_surveillance_to_booking_g1.csv')
        with open(filename, 'r') as f:
            lines = f.readlines()
            lines = lines[1:]
        subject_list = [int(x.split(',')[1]) for x in lines]
        subject_list = list(set(subject_list))
        for subject in subject_list:
            template = IJBSTemplate()
            template.subject_id = subject
            template.images = [x.split(',')[2] for x in lines if \
                int(x.split(',')[1]) == subject]
            self.all_template_list.append(template)
            self.protocol_template_indices['booking_image_gallery1'].append(index)
            index += 1
        self.protocol_template_indices['booking_image_gallery1'] = np.array(
            self.protocol_template_indices['booking_image_gallery1']).astype(
            int).flatten()

        self.protocol_template_indices['booking_image_gallery2'] = []
        filename = os.path.join(folder_ijbs_protocols,
            'galleries/cs6_surveillance_to_booking_g2.csv')
        with open(filename, 'r') as f:
            lines = f.readlines()
            lines = lines[1:]
        subject_list = [int(x.split(',')[1]) for x in lines]
        subject_list = list(set(subject_list))
        for subject in subject_list:
            template = IJBSTemplate()
            template.subject_id = subject
            template.images = [x.split(',')[2] for x in lines if \
                int(x.split(',')[1]) == subject]
            self.all_template_list.append(template)
            self.protocol_template_indices['booking_image_gallery2'].append(index)
            index += 1
        self.protocol_template_indices['booking_image_gallery2'] = np.array(
            self.protocol_template_indices['booking_image_gallery2']).astype(
            int).flatten()
        ############ Get Booking Image Templates ################

        self.all_template_list = np.array(self.all_template_list).astype(
            np.object).flatten()

    def save(self, filename):
        save_dict = {}
        save_dict['all_template_list'] = self.all_template_list
        save_dict['protocol_template_indices'] = self.protocol_template_indices
        save_dict['image_paths'] = self.image_paths
        with open(filename, 'wb') as f:
            pickle.dump(save_dict, f)

    def load(self, filename):
        with open(filename, 'rb') as f:
            save_dict = pickle.load(f)
        self.all_template_list = save_dict['all_template_list']
        self.protocol_template_indices = save_dict['protocol_template_indices']
        self.image_paths = save_dict['image_paths']

    def initialize_indices(self, image_paths):
        if type(image_paths) is str and os.path.isfile(image_paths):
            with open(image_paths, 'r') as f:
                lines = f.readlines()
                image_paths = [x.split('\n')[0] for x in lines]
        self.image_paths = np.array(image_paths).astype(np.object).flatten()

        path_dict = {}
        temp_dict = {}
        for i,path in enumerate(self.image_paths):
            subject_id = os.path.basename(os.path.dirname(path))
            filename = os.path.basename(path)
            if filename.startswith('videos'):
                temp = filename.split('_')
                video_name = str(subject_id)+'_'+temp[0]+'_'+temp[1]
                if video_name in path_dict:
                    path_dict[video_name].append(i)
                else:
                    path_dict[video_name] = [i]

                temp_name = temp[0]+'_'+temp[1]
                if temp_name not in temp_dict:
                    temp_dict[temp_name] = []
            if filename.startswith('img'):
                path_dict[str(subject_id)+'_'+os.path.splitext(filename)[0]] = [i]
                temp_dict[os.path.splitext(filename)[0]] = []

        none_dict = {}
        for i,template in enumerate(self.all_template_list):
            indices = []
            if template.videos is not None:
                for video in template.videos:
                    temp = video.split('/')
                    name = str(template.subject_id)+'_' \
                        +temp[0]+'_'+os.path.splitext(temp[1])[0]
                    if name in path_dict:
                        indices.extend(path_dict[name])
                    
                    temp_name = temp[0]+'_'+os.path.splitext(temp[1])[0]
                    if temp_name not in temp_dict:
                        if temp_name not in none_dict:
                            none_dict[temp_name] = []
            if template.images is not None:
                for image in template.images:
                    temp = image.split('/')
                    name = str(template.subject_id)+'_' \
                        +temp[0]+'_'+os.path.splitext(temp[1])[0]
                    if name in path_dict:
                        indices.extend(path_dict[name])
                    
                    temp_name = temp[0]+'_'+os.path.splitext(temp[1])[0]
                    if temp_name not in temp_dict:
                        if temp_name not in none_dict:
                            none_dict[temp_name] = []
            if len(indices) > 0:
                template.indices = np.array(indices).astype(int).flatten()
            else:
                template.indices = None

        keys = list(none_dict)
        print(keys)

    def identification(self, template_probe, template_gallery1, template_gallery2, get_retrievals=False):
        template_gallery = np.vstack((template_gallery1, template_gallery2))
        feature_probe = [x.feature for x in template_probe]
        label_probe = np.array([x.subject_id for x in template_probe]).reshape(-1,1)
        feature_gallery1 = [x.feature for x in template_gallery1]
        label_gallery1 = np.array([x.subject_id for x in template_gallery1]).reshape(-1,1)
        feature_gallery2 = [x.feature for x in template_gallery2]
        label_gallery2 = np.array([x.subject_id for x in template_gallery2]).reshape(-1,1)
        
        # Close-set 
        scores1 = self.compare_func(feature_probe, feature_gallery1)
        scores2 = self.compare_func(feature_probe, feature_gallery2)
        DIRs_closeset, _, _ = DIR_FAR(np.hstack((scores1, scores2)), label_probe==np.vstack((label_gallery1,
            label_gallery2)).T, ranks=[1, 5, 10])
        
        # Open-set
        # scores3 = self.compare_func(feature_gallery1, feature_gallery2)
        # scores4 = scores3.T
        DIRs_openset1, _, _ = DIR_FAR(scores1, 
            label_probe == label_gallery1.T, FARs=[0.01, 0.1])
        DIRs_openset2, _, _ = DIR_FAR(scores2,
            label_probe == label_gallery2.T, FARs=[0.01, 0.1])
        DIRs_openset = (DIRs_openset1 + DIRs_openset2) / 2.0
        
        # return DIRs_closeset, DIRs_openset

        if get_retrievals:
            _, _, _,  mate_indices, success, sort_idx_mat_m, sorted_score_mat_m = DIR_FAR(np.hstack((scores1, scores2)), 
                                label_probe==np.vstack((label_gallery1, label_gallery2)).T, ranks=[1], get_retrievals=True)

            return template_probe, template_gallery, mate_indices, success, sort_idx_mat_m, sorted_score_mat_m

        else:

            return DIRs_closeset, DIRs_openset
            

    def surveillance_to_single(self, get_retrievals=False):
        template_probe = self.all_template_list[self.protocol_template_indices[\
            'video2image_probe']]
        template_gallery1 = self.all_template_list[self.protocol_template_indices[\
            'single_image_gallery1']]
        template_gallery2 = self.all_template_list[self.protocol_template_indices[\
            'single_image_gallery2']]
        return self.identification(template_probe, 
            template_gallery1, template_gallery2, get_retrievals=get_retrievals)

    def surveillance_to_booking(self, get_retrievals=False):
        template_probe = self.all_template_list[self.protocol_template_indices[\
            'video2image_probe']]
        template_gallery1 = self.all_template_list[self.protocol_template_indices[\
            'booking_image_gallery1']]
        template_gallery2 = self.all_template_list[self.protocol_template_indices[\
            'booking_image_gallery2']]
        return self.identification(template_probe, 
            template_gallery1, template_gallery2, get_retrievals=get_retrievals)


    def multiview_surveillance_to_booking(self, get_retrievals=False):
        template_probe = self.all_template_list[self.protocol_template_indices[\
            'multiview_video_probe']]
        template_gallery1 = self.all_template_list[self.protocol_template_indices[\
            'booking_image_gallery1']]
        template_gallery2 = self.all_template_list[self.protocol_template_indices[\
            'booking_image_gallery2']]
        return self.identification(template_probe, 
            template_gallery1, template_gallery2, get_retrievals=get_retrievals)


    def surveillance_to_surveillance(self, get_retrievals=False):
        template_probe = self.all_template_list[self.protocol_template_indices[\
            'video2video_probe']]
        template_gallery1 = self.all_template_list[self.protocol_template_indices[\
            'video_gallery1']]
        template_gallery2 = self.all_template_list[self.protocol_template_indices[\
            'video_gallery2']]
        return self.identification(template_probe, 
                template_gallery1, template_gallery2, get_retrievals=get_retrievals)


    def uav_surveillance_to_booking(self, get_retrievals=False):
        template_probe = self.all_template_list[self.protocol_template_indices[\
            'uav_probe']]
        template_gallery1 = self.all_template_list[self.protocol_template_indices[\
            'booking_image_gallery1']]
        template_gallery2 = self.all_template_list[self.protocol_template_indices[\
            'booking_image_gallery2']]
        return self.identification(template_probe, 
                template_gallery1, template_gallery2, get_retrievals=get_retrievals)



def comparePairs(template_pairs, metricFunc, num_proc=8, log_info=False):
    proc_list = []
    result_array = multiprocessing.Array('f', len(template_pairs))
    print('# of pairs: %d' % len(template_pairs))
    def proc_job(pairs, start_idx, result_array):
        for i,pair in enumerate(pairs):
            if log_info and (i % len(pairs)//10) == 0:
                print('Comparing row: %d' % (start_idx+i))
            score = metricFunc(pair[0], pair[1])
            result_array[start_idx+i] = score

    split_size = len(template_pairs) // num_proc
    for i in range(num_proc):
        start_idx = i * split_size
        end_idx = len(template_pairs) if i==num_proc-1 else (i+1) * split_size
        p = multiprocessing.Process(target=proc_job, args=(template_pairs[start_idx:end_idx], start_idx, result_array))
        p.start()
        proc_list.append(p)
    for p in proc_list:
        p.join()

    scores = np.array(result_array)
    return scores



def DIR_FAR(score_mat, label_mat, ranks=[1], FARs=[1.0], get_retrievals=False):
    ''' Closed/Open-set Identification. 
        A general case of Cummulative Match Characteristic (CMC) 
        where thresholding is allowed for open-set identification.
    args:
        score_mat:            a P x G matrix, P is number of probes, G is size of gallery
        label_mat:            a P x G matrix, bool
        ranks:                a list of integers
        FARs:                 false alarm rates, if 1.0, closed-set identification (CMC)
        get_retrievals:       not implemented yet
    return:
        DIRs:                 an F x R matrix, F is the number of FARs, R is the number of ranks, 
                              flatten into a vector if F=1 or R=1.
        FARs:                 an vector of length = F.
        thredholds:           an vector of length = F.
    '''
    assert score_mat.shape==label_mat.shape
    assert np.all(label_mat.astype(np.float32).sum(axis=1) <=1 )
    # Split the matrix for match probes and non-match probes
    # subfix _m: match, _nm: non-match
    # For closed set, we only use the match probes
    mate_indices = label_mat.astype(np.bool).any(axis=1)
    score_mat_m = score_mat[mate_indices,:]
    label_mat_m = label_mat[mate_indices,:]
    score_mat_nm = score_mat[np.logical_not(mate_indices),:]
    label_mat_nm = label_mat[np.logical_not(mate_indices),:]
    mate_indices = np.argwhere(mate_indices).flatten()

    # print('mate probes: %d, non mate probes: %d' % (score_mat_m.shape[0], score_mat_nm.shape[0]))

    # Find the thresholds for different FARs
    max_score_nm = np.max(score_mat_nm, axis=1)
    label_temp = np.zeros(max_score_nm.shape, dtype=np.bool)
    if len(FARs) == 1 and FARs[0] >= 1.0:
        # If only testing closed-set identification, use the minimum score as thrnp.vstack((eshold
        # in case there is no non-mate probes
        thresholds = [np.min(score_mat) - 1e-10]
    else:
        # If there is open-set identification, find the thresholds by FARs.
        assert score_mat_nm.shape[0] > 0, "For open-set identification (FAR<1.0), there should be at least one non-mate probe!"
        thresholds = find_thresholds_by_FAR(max_score_nm, label_temp, FARs=FARs)

    # Sort the labels row by row according to scores
    sort_idx_mat_m = np.argsort(score_mat_m, axis=1)[:,::-1]
    sorted_label_mat_m = np.ndarray(label_mat_m.shape, dtype=np.bool)
    sorted_score_mat_m = score_mat_m.copy()
    for row in range(label_mat_m.shape[0]):
        sort_idx = (sort_idx_mat_m[row, :])
        sorted_label_mat_m[row,:] = label_mat_m[row, sort_idx]
        sorted_score_mat_m[row,:] = score_mat_m[row, sort_idx]
        
    # Calculate DIRs for different FARs and ranks
    gt_score_m = score_mat_m[label_mat_m]
    assert gt_score_m.size == score_mat_m.shape[0]

    DIRs = np.zeros([len(FARs), len(ranks)], dtype=np.float32)
    FARs = np.zeros([len(FARs)], dtype=np.float32)
    success = np.ndarray((len(FARs), len(ranks)), dtype=np.object)
    for i, threshold in enumerate(thresholds):
        for j, rank  in enumerate(ranks):
            score_rank = gt_score_m >= threshold
            retrieval_rank = sorted_label_mat_m[:,0:rank].any(axis=1)
            DIRs[i,j] = (score_rank & retrieval_rank).astype(np.float32).mean()
            if get_retrievals:
                success[i,j] = (score_rank & retrieval_rank)
        if score_mat_nm.shape[0] > 0:
            FARs[i] = (max_score_nm >= threshold).astype(np.float32).mean()

    if DIRs.shape[0] == 1 or DIRs.shape[1] == 1:
        DIRs = DIRs.flatten()
        success = success.flatten()

    if get_retrievals:
        return DIRs, FARs, thresholds, mate_indices, success, sort_idx_mat_m, sorted_score_mat_m

    return DIRs, FARs, thresholds


def find_thresholds_by_FAR(score_vec, label_vec, FARs=None, epsilon=1e-8):
    assert len(score_vec.shape)==1
    assert score_vec.shape == label_vec.shape
    assert label_vec.dtype == np.bool
    score_neg = score_vec[~label_vec]
    score_neg = np.sort(score_neg)[::-1] # score from high to low
    num_neg = len(score_neg)

    assert num_neg >= 1

    if FARs is None:
        epsilon = 1e-5
        thresholds = np.unique(score_neg)
        thresholds = np.insert(thresholds, 0, thresholds[0]+epsilon)
        thresholds = np.insert(thresholds, thresholds.size, thresholds[-1]-epsilon)
    else:
        FARs = np.array(FARs)
        num_false_alarms = (num_neg * FARs).astype(np.int32)

        thresholds = []
        for num_false_alarm in num_false_alarms:
            if num_false_alarm==0:
                threshold = score_neg[0] + epsilon
            else:
                threshold = score_neg[num_false_alarm-1]
            thresholds.append(threshold)
        thresholds = np.array(thresholds)

    return thresholds
