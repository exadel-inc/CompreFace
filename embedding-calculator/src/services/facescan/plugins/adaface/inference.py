import src.services.facescan.plugins.adaface.net as net
import torch
import os
from src.services.facescan.plugins.adaface.face_alignment import align
import numpy as np
from src.constants import ENV

device = ENV.DEVICE
model_mame = ENV.RECOGNITION_MODEL
model_path = os.path.join(os.getcwd(), ENV.RECOGNITION_MODEL_PATH)

detector_model_name = ENV.DETECTOR_NAME

adaface_models = {
    model_mame:model_path,
}

preloaded = ""

def load_pretrained_model(architecture=model_mame):
    global preloaded

    if preloaded == "":
        # load model and pretrained statedict
        assert architecture in adaface_models.keys()
        preloaded = net.build_model(architecture)
        print("load adaface model")
        statedict = torch.load(adaface_models[architecture],map_location =device)['state_dict']
        model_statedict = {key[6:]:val for key, val in statedict.items() if key.startswith('model.')}
        preloaded.load_state_dict(model_statedict)
        preloaded.eval()
    return preloaded

def to_input(pil_rgb_image):
    np_img = np.array(pil_rgb_image)
    brg_img = ((np_img[:,:,::-1] / 255.) - 0.5) / 0.5
    tensor = torch.tensor([brg_img.transpose(2,0,1)]).float()
    return tensor

def inference_detector(image_path):
    model = load_pretrained_model(model_mame)
    feature, norm = model(torch.randn(2,3,112,112))
    detected = {"plugins_versions": {"calculator": model_mame,
                                       "detector": detector_model_name},
                  "result": []}
    features = []
    aligned_rgb_images, bboxes = align.get_aligned_face(image_path)
    for aligned_rgb_img, box in zip(aligned_rgb_images, bboxes):
        bgr_tensor_input = to_input(aligned_rgb_img)
        feature, _ = model(bgr_tensor_input)
        feature = feature.tolist()
        features.append(feature)
        face = {"box": {"probability": box[4],
                    "x_max": box[2],
                    "x_min": box[0],
                    "y_max": box[3],
                    "y_min": box[1], },
            "embedding": feature[0]}

        detected["result"].append(face)

    print(f'Processed {len(detected["result"])} faces')
    return detected

def inference_scaner(image_path):

    model = load_pretrained_model(model_mame)
    feature, norm = model(torch.randn(2,3,112,112))
    detected = {"calculator_version": model_mame,
                  "result": []}
    features = []
    aligned_rgb_images, bboxes = align.get_aligned_face(image_path)
    for aligned_rgb_img, box in zip(aligned_rgb_images, bboxes):
        bgr_tensor_input = to_input(aligned_rgb_img)
        feature, _ = model(bgr_tensor_input)
        feature = feature.tolist()
        features.append(feature)
        face = {"box": {"probability": box[4],
                    "x_max": box[2],
                    "x_min": box[0],
                    "y_max": box[3],
                    "y_min": box[1], },
            "embedding": feature}

        detected["result"].append(face)

    return detected
    

