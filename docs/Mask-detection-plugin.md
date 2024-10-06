# Face mask detection plugin

A Mask detection plugin can be used to detect if the person wears a mask
correctly automatically. There are three possible results:
`without_mask`, `mask_worn_incorrectly`, `mask_worn_correctly`.

There was no suitable free and ready-to-use model for face mask
detection at the moment of adding this plugin, so we created our model.

    Disclaimer:
    Software developers, not medical experts, created the plugin. 
    The plugin doesn't contain the recommendations of how to use and wear face mask correctly.
    The accuracy of the model is not 100%.
    Please use the plugin at your own risk.

# Face mask detection example

![results](https://user-images.githubusercontent.com/3736126/130656086-3167421e-f697-4837-8cf9-e3889d49a44d.png)

# Training process

## Dataset

We used four publicly available datasets for training the model:

1.  [Kaggle face mask detection
    dataset](https://www.kaggle.com/andrewmvd/face-mask-detection)
2.  [Kaggle medical masks dataset images
    tfrecords](https://www.kaggle.com/ivandanilovich/medical-masks-dataset-images-tfrecords)
3.  [Kaggle face mask detection dataset
    \#2](https://www.kaggle.com/wobotintelligence/face-mask-detection-dataset?select=train.csv)
4.  [MAFA
    dataset](https://drive.google.com/drive/folders/1nbtM1n0--iZ3VVbNGhocxbnBGhMau_OG)

We extracted faces with masks from the first dataset (around 4k images),
faces without a mask from the first two datasets (around 4k images),
faces with masks worn incorrectly from all four datasets (around 2k
images). Then we duplicated each incorrect worn mask image with data
augmentation (see augmentation.py) to achieve class balance.

## Train

InceptionV3 was cut off on a mixed 7 layer to improve speed and was used
as a backbone. The final model with 97.2 % accuracy is used by default
and can be found
[here](https://drive.google.com/file/d/1jm2Wd2JEZxhS8O1JjV-kfBOyOYUMxKHq/view?usp=sharing)
