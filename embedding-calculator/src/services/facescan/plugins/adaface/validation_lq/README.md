
#IJB-S Evaluation

The distribution of IJB-S dataset is managed by the authors. 
Request dataset from the dataset authors. 

http://biometrics.cse.msu.edu/Publications/Face/Kalkaetal_IJBSIARPPAJanusSurveillanceVideoBenchmark_BTAS2018.pdf

Preprocess the dataset by
1. loosely cropping the faces with ground truth bounding boxes and organize them as
```
For videos: 
<PID>/videos_<VideoID>_<FRAME>.jpg
EX)
1/videos_5004_21750.jpg
1/videos_5004_21751.jpg
...

For images:
<PID>/img_<FRAME>.jpg
EX)
1/img_101146.jpg
1/img_101147.jpg
...
```

* Note that it works better to pad the ground truth bounding box by 10% and crop it to 256x256 for subsequent MTCNN alignment. 
Refer to the code snippet for loose cropping. 

```
def square_bbox(bbox):
    '''Output a square-like bounding box. But because all the numbers are float, 
    it is not guaranteed to really be a square.'''
    x, y, w, h = tuple(bbox)
    cx = x + 0.5 * w
    cy = y + 0.5 * h
    _w = _h = max(w, h)
    _x = cx - 0.5*_w
    _y = cy - 0.5*_h
    return (_x, _y, _w, _h)
    
def pad_bbox(bbox, padding_ratio):
    x, y, w, h = tuple(bbox)
    pad_x = padding_ratio * w
    pad_y = padding_ratio * h
    return (x-pad_x, y-pad_y, w+2*pad_x, h+2*pad_y)

def crop(image, bbox):
    rint = lambda a: int(round(a))
    x, y, w, h = tuple(map(rint, bbox))
    safe_pad = max(0, -x ,-y, x+w-image.shape[1], y+h-image.shape[0])
    img = np.zeros((image.shape[0]+2*safe_pad, image.shape[1]+2*safe_pad, image.shape[2]))
    img[safe_pad:safe_pad+image.shape[0], safe_pad:safe_pad+image.shape[1], :] = image
    img = img[safe_pad+y : safe_pad+y+h, safe_pad+x : safe_pad+x+w, :]
    return img

# example 
padding_ratio = 0.1
target_size = (256, 256)
bbox = tuple(map(float,parts[2:6]))
bbox = square_bbox(bbox)
bbox = pad_bbox(bbox, padding_ratio)
img_cropped = crop(img, bbox)
img_cropped = cv2.resize(img_cropped, target_size)
```

2. Align the loose crop faces with face alignment tool such as MTCNN. And save it in the same structure.
- MTCNN may fail to align many images. We used only the successful images and discarded the alignment failed frames. 
- For sanity checking, refer to the list of successful frames in [link](https://drive.google.com/file/d/1krZDlYVvj64EhnkXRnpquT8w9Drp4rGo/view?usp=sharing)
- Depending on the alignment methods and implementations, the aligned result might vary slightly.

4. Run
```
python validate_IJB_S.py --data_root $DATA_ROOT --model_name ir50
```


# TinyFace Evaluation

1. Download the evaluation split from https://qmul-tinyface.github.io/
   1. Extract the zip file under `<data_root>`. 
2. TinyFace Evaluation images have to be aligned and resized. 
   1. You may perform the alignment yourself with MTCNN or download by completing this form [link](https://forms.gle/Mz1LNrQwn1Bwjvo86).
   2. Do not re-distribute the aligned data. It is released for encouraging reproducibility of research. 
   But if it infringes the copy-right of the original tinyface dataset, the aligned version may be retracted.
   5. Extract the zip file under `<data_root>`
3. You may run evaluation with the example script below.

```
python validate_tinyface.py --data_root <data_root> --model_name ir101_webface4m
```