import pandas as pd
import os
import numpy as np
import random
import cv2
from skimage import transform
from sklearn.metrics import roc_curve, auc
from tqdm import tqdm
import matplotlib.pyplot as plt

def read_IJB_meta_columns_to_int(file_path, columns, sep=" ", skiprows=0, header=None):
    # meta = np.loadtxt(file_path, skiprows=skiprows, delimiter=sep)
    meta = pd.read_csv(file_path, sep=sep, skiprows=skiprows, header=header).values
    return (meta[:, ii].astype("int") for ii in columns)


def extract_IJB_data_11(data_path, subset, save_path=None, force_reload=False):
    if save_path == None:
        save_path = os.path.join(data_path, subset + "_backup.npz")
    if not force_reload and os.path.exists(save_path):
        print(">>>> Reload from backup: %s ..." % save_path)
        aa = np.load(save_path)
        return (
            aa["templates"],
            aa["medias"],
            aa["p1"],
            aa["p2"],
            aa["label"],
            aa["img_names"],
            aa["landmarks"],
            aa["face_scores"],
        )

    if subset == "IJBB":
        media_list_path = os.path.join(data_path, "IJBB/meta/ijbb_face_tid_mid.txt")
        pair_list_path = os.path.join(data_path, "IJBB/meta/ijbb_template_pair_label.txt")
        img_path = os.path.join(data_path, "IJBB/loose_crop")
        img_list_path = os.path.join(data_path, "IJBB/meta/ijbb_name_5pts_score.txt")
    else:
        media_list_path = os.path.join(data_path, "IJBC/meta/ijbc_face_tid_mid.txt")
        pair_list_path = os.path.join(data_path, "IJBC/meta/ijbc_template_pair_label.txt")
        img_path = os.path.join(data_path, "IJBC/loose_crop")
        img_list_path = os.path.join(data_path, "IJBC/meta/ijbc_name_5pts_score.txt")

    print(">>>> Loading templates and medias...")
    templates, medias = read_IJB_meta_columns_to_int(media_list_path, columns=[1, 2])  # ['1.jpg', '1', '69544']
    print("templates: %s, medias: %s, unique templates: %s" % (templates.shape, medias.shape, np.unique(templates).shape))
    # templates: (227630,), medias: (227630,), unique templates: (12115,)

    print(">>>> Loading pairs...")
    p1, p2, label = read_IJB_meta_columns_to_int(pair_list_path, columns=[0, 1, 2])  # ['1', '11065', '1']
    print("p1: %s, unique p1: %s" % (p1.shape, np.unique(p1).shape))
    print("p2: %s, unique p2: %s" % (p2.shape, np.unique(p2).shape))
    print("label: %s, label value counts: %s" % (label.shape, dict(zip(*np.unique(label, return_counts=True)))))
    # p1: (8010270,), unique p1: (1845,)
    # p2: (8010270,), unique p2: (10270,) # 10270 + 1845 = 12115 --> np.unique(templates).shape
    # label: (8010270,), label value counts: {0: 8000000, 1: 10270}

    print(">>>> Loading images...")
    with open(img_list_path, "r") as ff:
        # 1.jpg 46.060 62.026 87.785 60.323 68.851 77.656 52.162 99.875 86.450 98.648 0.999
        img_records = np.array([ii.strip().split(" ") for ii in ff.readlines()])

    img_names = np.array([os.path.join(img_path, ii) for ii in img_records[:, 0]])
    landmarks = img_records[:, 1:-1].astype("float32").reshape(-1, 5, 2)
    face_scores = img_records[:, -1].astype("float32")
    print("img_names: %s, landmarks: %s, face_scores: %s" % (img_names.shape, landmarks.shape, face_scores.shape))
    # img_names: (227630,), landmarks: (227630, 5, 2), face_scores: (227630,)
    print("face_scores value counts:", dict(zip(*np.histogram(face_scores, bins=9)[::-1])))
    # {0.1: 2515, 0.2: 0, 0.3: 62, 0.4: 94, 0.5: 136, 0.6: 197, 0.7: 291, 0.8: 538, 0.9: 223797}

    print(">>>> Saving backup to: %s ..." % save_path)
    np.savez(
        save_path,
        templates=templates,
        medias=medias,
        p1=p1,
        p2=p2,
        label=label,
        img_names=img_names,
        landmarks=landmarks,
        face_scores=face_scores,
    )
    print()
    return templates, medias, p1, p2, label, img_names, landmarks, face_scores


def extract_gallery_prob_data(data_path, subset, save_path=None, force_reload=False):
    if save_path == None:
        save_path = os.path.join(data_path, subset + "_gallery_prob_backup.npz")
    if not force_reload and os.path.exists(save_path):
        print(">>>> Reload from backup: %s ..." % save_path)
        aa = np.load(save_path)
        return (
            aa["s1_templates"],
            aa["s1_subject_ids"],
            aa["s2_templates"],
            aa["s2_subject_ids"],
            aa["probe_mixed_templates"],
            aa["probe_mixed_subject_ids"],
        )

    if subset == "IJBC":
        meta_dir = os.path.join(data_path, "IJBC/meta")
        gallery_s1_record = os.path.join(meta_dir, "ijbc_1N_gallery_G1.csv")
        gallery_s2_record = os.path.join(meta_dir, "ijbc_1N_gallery_G2.csv")
        probe_mixed_record = os.path.join(meta_dir, "ijbc_1N_probe_mixed.csv")
    else:
        meta_dir = os.path.join(data_path, "IJBB/meta")
        gallery_s1_record = os.path.join(meta_dir, "ijbb_1N_gallery_S1.csv")
        gallery_s2_record = os.path.join(meta_dir, "ijbb_1N_gallery_S2.csv")
        probe_mixed_record = os.path.join(meta_dir, "ijbb_1N_probe_mixed.csv")

    print(">>>> Loading gallery feature...")
    s1_templates, s1_subject_ids = read_IJB_meta_columns_to_int(gallery_s1_record, columns=[0, 1], skiprows=1, sep=",")
    s2_templates, s2_subject_ids = read_IJB_meta_columns_to_int(gallery_s2_record, columns=[0, 1], skiprows=1, sep=",")
    print("s1 gallery: %s, ids: %s, unique: %s" % (s1_templates.shape, s1_subject_ids.shape, np.unique(s1_templates).shape))
    print("s2 gallery: %s, ids: %s, unique: %s" % (s2_templates.shape, s2_subject_ids.shape, np.unique(s2_templates).shape))

    print(">>>> Loading prope feature...")
    probe_mixed_templates, probe_mixed_subject_ids = read_IJB_meta_columns_to_int(
        probe_mixed_record, columns=[0, 1], skiprows=1, sep=","
    )
    print("probe_mixed_templates: %s, unique: %s" % (probe_mixed_templates.shape, np.unique(probe_mixed_templates).shape))
    print("probe_mixed_subject_ids: %s, unique: %s" % (probe_mixed_subject_ids.shape, np.unique(probe_mixed_subject_ids).shape))

    print(">>>> Saving backup to: %s ..." % save_path)
    np.savez(
        save_path,
        s1_templates=s1_templates,
        s1_subject_ids=s1_subject_ids,
        s2_templates=s2_templates,
        s2_subject_ids=s2_subject_ids,
        probe_mixed_templates=probe_mixed_templates,
        probe_mixed_subject_ids=probe_mixed_subject_ids,
    )
    print()
    return s1_templates, s1_subject_ids, s2_templates, s2_subject_ids, probe_mixed_templates, probe_mixed_subject_ids


def face_align_landmark(img, landmark, image_size=(112, 112), method="similar"):
    tform = transform.AffineTransform() if method == "affine" else transform.SimilarityTransform()
    src = np.array(
        [[38.2946, 51.6963], [73.5318, 51.5014], [56.0252, 71.7366], [41.5493, 92.3655], [70.729904, 92.2041]], dtype=np.float32
    )
    tform.estimate(landmark, src)
    # ndimage = transform.warp(img, tform.inverse, output_shape=image_size)
    # ndimage = (ndimage * 255).astype(np.uint8)
    M = tform.params[0:2, :]
    ndimage = cv2.warpAffine(img, M, image_size, borderValue=0.0)
    if len(ndimage.shape) == 2:
        ndimage = np.stack([ndimage, ndimage, ndimage], -1)
    else:
        if random.random() < 0.1:
            print('using RGB image!!')
        ndimage = cv2.cvtColor(ndimage, cv2.COLOR_BGR2RGB)
    return ndimage


def plot_roc_and_calculate_tpr(scores, names=None, label=None, save_root=''):
    print(">>>> plot roc and calculate tpr...")
    score_dict = {}
    for id, score in enumerate(scores):
        name = None if names is None else names[id]
        if isinstance(score, str) and score.endswith(".npz"):
            aa = np.load(score)
            score = aa.get("scores", [])
            label = aa["label"] if label is None and "label" in aa else label
            score_name = aa.get("names", [])
            for ss, nn in zip(score, score_name):
                score_dict[nn] = ss
        elif isinstance(score, str) and score.endswith(".npy"):
            name = name if name is not None else os.path.splitext(os.path.basename(score))[0]
            score_dict[name] = np.load(score)
        elif isinstance(score, str) and score.endswith(".txt"):
            # IJB meta data like ijbb_template_pair_label.txt
            label = pd.read_csv(score, sep=" ", header=None).values[:, 2]
        else:
            name = name if name is not None else str(id)
            score_dict[name] = score
    if label is None:
        print("Error: Label data is not provided")
        return None, None

    x_labels = [10 ** (-ii) for ii in range(1, 7)[::-1]]
    fpr_dict, tpr_dict, roc_auc_dict, tpr_result = {}, {}, {}, {}
    for name, score in score_dict.items():
        fpr, tpr, _ = roc_curve(label, score)
        roc_auc = auc(fpr, tpr)
        fpr, tpr = np.flipud(fpr), np.flipud(tpr)  # select largest tpr at same fpr
        tpr_result[name] = [tpr[np.argmin(abs(fpr - ii))] for ii in x_labels]
        fpr_dict[name], tpr_dict[name], roc_auc_dict[name] = fpr, tpr, roc_auc
    tpr_result_df = pd.DataFrame(tpr_result, index=x_labels).T
    tpr_result_df['AUC'] = pd.Series(roc_auc_dict)
    tpr_result_df.columns.name = "Methods"
    print(tpr_result_df)
    # print(tpr_result_df)

    fig = plt.figure()
    for name in score_dict:
        plt.plot(fpr_dict[name], tpr_dict[name], lw=1, label="[%s (AUC = %0.4f%%)]" % (name, roc_auc_dict[name] * 100))
    title = "ROC on IJB" + name.split("IJB")[-1][0] if "IJB" in name else "ROC on IJB"

    plt.xlim([10 ** -6, 0.1])
    plt.xscale("log")
    plt.xticks(x_labels)
    plt.xlabel("False Positive Rate")
    plt.ylim([0.3, 1.0])
    plt.yticks(np.linspace(0.3, 1.0, 8, endpoint=True))
    plt.ylabel("True Positive Rate")

    plt.grid(linestyle="--", linewidth=1)
    plt.title(title)
    plt.legend(loc="lower right", fontsize='x-small')
    plt.tight_layout()

    if save_root:
        plt.savefig(os.path.join(save_root, 'visual.png'))
        tpr_result_df.to_csv(os.path.join(save_root, 'result.csv'))
    plt.clf() 

    return tpr_result_df, fig


def plot_dir_far_cmc_scores(scores, names=None, save_root=''):
    fig = plt.figure()
    for id, score in enumerate(scores):
        name = None if names is None else names[id]
        if isinstance(score, str) and score.endswith(".npz"):
            aa = np.load(score)
            score, name = aa.get("scores")[0], aa.get("names")[0]
        fars, tpirs = score[0], score[1]
        name = name if name is not None else str(id)

        auc_value = auc(fars, tpirs)
        label = "[%s (AUC = %0.4f%%)]" % (name, auc_value * 100)
        plt.plot(fars, tpirs, lw=1, label=label)

    plt.xlabel("False Alarm Rate")
    plt.xlim([0.0001, 1])
    plt.xscale("log")
    plt.ylabel("Detection & Identification Rate (%)")
    plt.ylim([0, 1])

    plt.grid(linestyle="--", linewidth=1)
    plt.legend(fontsize='x-small')
    plt.tight_layout()
    
    if save_root:
        plt.savefig(os.path.join(save_root, 'visual.png'))
    plt.clf()

    return fig



def verification_11(template_norm_feats=None, unique_templates=None, p1=None, p2=None, batch_size=10000):
    try:
        print(">>>> Trying cupy.")
        import cupy as cp

        template_norm_feats = cp.array(template_norm_feats)
        score_func = lambda feat1, feat2: cp.sum(feat1 * feat2, axis=-1).get()
        test = score_func(template_norm_feats[:batch_size], template_norm_feats[:batch_size])
    except:
        score_func = lambda feat1, feat2: np.sum(feat1 * feat2, -1)

    template2id = np.zeros(max(unique_templates) + 1, dtype=int)
    template2id[unique_templates] = np.arange(len(unique_templates))

    steps = int(np.ceil(len(p1) / batch_size))
    score = []
    for id in tqdm(range(steps), "Verification"):
        feat1 = template_norm_feats[template2id[p1[id * batch_size : (id + 1) * batch_size]]]
        feat2 = template_norm_feats[template2id[p2[id * batch_size : (id + 1) * batch_size]]]
        score.extend(score_func(feat1, feat2))
    return np.array(score)


def evaluation_1N(query_feats, gallery_feats, query_ids, reg_ids, fars=[0.01, 0.1]):
    print("query_feats: %s, gallery_feats: %s" % (query_feats.shape, gallery_feats.shape))
    similarity = np.dot(query_feats, gallery_feats.T)  # (19593, 3531)

    top_1_count, top_5_count, top_10_count = 0, 0, 0
    pos_sims, neg_sims, non_gallery_sims = [], [], []
    for index, query_id in enumerate(query_ids):
        if query_id in reg_ids:
            gallery_label = np.argwhere(reg_ids == query_id)[0, 0]
            index_sorted = np.argsort(similarity[index])[::-1]

            top_1_count += gallery_label in index_sorted[:1]
            top_5_count += gallery_label in index_sorted[:5]
            top_10_count += gallery_label in index_sorted[:10]

            pos_sims.append(similarity[index][reg_ids == query_id][0])
            neg_sims.append(similarity[index][reg_ids != query_id])
        else:
            non_gallery_sims.append(similarity[index])
    total_pos = len(pos_sims)
    pos_sims, neg_sims, non_gallery_sims = np.array(pos_sims), np.array(neg_sims), np.array(non_gallery_sims)
    print("pos_sims: %s, neg_sims: %s, non_gallery_sims: %s" % (pos_sims.shape, neg_sims.shape, non_gallery_sims.shape))
    print("top1: %f, top5: %f, top10: %f" % (top_1_count / total_pos, top_5_count / total_pos, top_10_count / total_pos))

    correct_pos_cond = pos_sims > neg_sims.max(1)
    non_gallery_sims_sorted = np.sort(non_gallery_sims.max(1))[::-1]
    threshes, recalls = [], []
    for far in fars:
        # thresh = non_gallery_sims_sorted[int(np.ceil(non_gallery_sims_sorted.shape[0] * far)) - 1]
        thresh = non_gallery_sims_sorted[max(int((non_gallery_sims_sorted.shape[0]) * far) - 1, 0)]
        recall = np.logical_and(correct_pos_cond, pos_sims > thresh).sum() / pos_sims.shape[0]
        threshes.append(thresh)
        recalls.append(recall)
        # print("FAR = {:.10f} TPIR = {:.10f} th = {:.10f}".format(far, recall, thresh))
    cmc_scores = list(zip(neg_sims, pos_sims.reshape(-1, 1))) + list(zip(non_gallery_sims, [None] * non_gallery_sims.shape[0]))
    return top_1_count, top_5_count, top_10_count, threshes, recalls, cmc_scores
