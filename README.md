# CompreFace

[![GitHub license](https://img.shields.io/github/license/exadel-inc/face-recognition-service)](https://www.apache.org/licenses/LICENSE-2.0) [![GitHub contributors](https://img.shields.io/github/contributors/exadel-inc/face-recognition-service)](https://github.com/exadel-inc/face-recognition-service/graphs/contributors)



 [CompreFace](#compreface)
  * [Overview](#overview)
  * [Features](#features)
  * [Getting started](#getting-started)
  * [Simple tutorial of usage](#simple-tutorial-of-usage)
  * [How it works](#how-it-works)
    + [ML technologies](#ml-technologies)
    + [Used ML Papers and Algorithms](#used-ml-papers-and-algorithms)
  * [Technologies](#technologies)
    + [Architecture diagram](#architecture-diagram)
    + [Database](#database)
    + [Platform server](#platform-server)
    + [API server](#api-server)
    + [Embedding server](#embedding-server)
  * [Rest API description](#rest-api-description)
    + [Add an example of the face](#add-an-example-of-the-face)
    + [Recognize faces from given image](#recognize-faces-from-given-image)
    + [List names of all saved faces](#list-names-of-all-saved-faces)
    + [Delete all examples of the face by name](#delete-all-examples-of-the-face-by-name)
  * [Contributing](#contributing)
    + [Formatting standards](#formatting-standards)
    + [Report Bugs](#report-bugs)
    + [Submit Feedback](#submit-feedback)
  * [License info](#license)



## Overview

CompreFace is the application for face recognition that can be integrated as a standalone server or deployed on cloud, and can be set up and used without machine learning knowledge. 

We use one of the most popular face recognition methods based on [deep neural networks](#used-ml-papers-and-algorithms), and provide a convenient API for model training and face recognition. We also provide a convenient roles system with which you can easily control who has access to the model.

Every user can create several models trained on different subsets of people. 



## Features

The system shows sufficient accuracy even if only one example for each face is used.

CompreFace is:

- opensource code and fully on-premise (security of your data)
- can be set up and used without machine learning knowledge
- used one of the most popular face recognition methods with high accuracy face recognizing
- UI panel with roles for access control
- fast start with one docker command



## Getting started

To get started, perform the following steps:

1. install Docker
1. just run command:
`
docker-compose up --build
`
1. open http://localhost:8000/



## Simple tutorial of usage

1. Registration users in the app
1. Creating applications, models, inviting users
1. Integrating your app via API if need
1. Images uploading, training a model with your own images by using the API key
1. Send a new image to recognize the face on it.

![how-it-works](https://user-images.githubusercontent.com/4942439/92221961-b3baa180-eeb7-11ea-89c9-af2bec2295fc.png)



## How it works

**Finding a face** 

Detecting one or more faces in an image. Multi-task Cascaded Convolutional Neural Networks (MTCNN) was used for face recognition.

**Posing and projecting faces** 

Normalization of all found faces with rotate, scale and shear. 

**Calculate embedding from faces**

Calculating embedding and classifying the face based on extracted features. We took CNN for face recognition and the last 3 fully connected layers were removed. As a result, - NN calculates embedding. 

**Use embedding for training model/recognize face using embedding**

Recognizing the person in the photo. Haifengl/smile [LogisticRegression](http://haifengl.github.io/api/java/smile/classification/LogisticRegression.html) as a classifier was used.



### ML technologies

* MTCNN (Multi-task Cascaded Convolutional Networks)
* Logistic Regression
* Transfer learning



### Used ML Papers and Algorithms

* **FaceNet: A Unified Embedding for Face Recognition and Clustering** 
  Florian Schroff, Dmitry Kalenichenko, James Philbin 
  (Submitted on 17 Jun 2015)

* **Joint Face Detection and Alignment using Multi-task Cascaded Convolutional Neural Networks**
  Kaipeng Zhang, Zhanpeng Zhang, Zhifeng Li, Yu Qiao 
  (Submitted on 11 Apr 2016)

* **Inception-v4, Inception-ResNet and the Impact of Residual Connections on Learning**
  Christian Szegedy, Sergey Ioffe, Vincent Vanhoucke, Alex Alemi 
  (Submitted on 23 Aug 2016)



## Technologies

### Architecture diagram



![architecture](https://user-images.githubusercontent.com/4942439/87042452-220f1a80-c20d-11ea-96ee-5f7ad6ddf93b.jpg)



### Database

* PostgreSQL



### Platform server

* Java 11
* Spring Boot

  

### API server

* Java 11
* Spring Boot
* Haifengl/Smile



### Embedding server

* Python
* [FaceNet](https://github.com/davidsandberg/facenet)
* [InsightFace](https://github.com/deepinsight/insightface)
* TensorFlow
* SciPy
* NumPy
* OpenCV (for images resizing)



## Rest API description

By using the created API key, the user can add an image as an example of the face, retrieve a list of saved images, recognize a face from the uploaded image, retrain the model, and delete all examples of the face by the name.



### Add an example of the face

Creates an example of the face by saving images. To train the system, you can add as many images as you want.

```http request
curl  -X POST "http://localhost:8000/api/v1/faces/?subject=<face_name>&retrain=<retrain_option>" \
-H "Content-Type: multipart/form-data" \
-H "x-api-key: <model_api_key>" \
-F file=@<local_file> \
-F det_prob_threshold=@<det_prob_threshold> \
-F retrain=@<retrain_option> \
```
| Element             | Description | Type   | Required | Notes                                                        |
| ------------------- | ----------- | ------ | -------- | ------------------------------------------------------------ |
| Content-Type        | header      | string | required | multipart/form-data                                          |
| x-api-key           | header      | string | required | api key of the model, created by the user                    |
| face_name           | param       | string | required | is the name you assign to the image you save                 |
| file                | body        | image  | required | allowed image formats: jpeg, jpg, ico, png, bmp, gif, tif, tiff, webp. Max size is 5Mb |
| det_prob_ threshold | body        | string | optional | minimum required confidence that a recognized face is actually a face. Value is between 0.0 and 1.0 |
| retrain_option      | body        | string | optional | retrains the model for the specified API key. Can be set to "yes", "no", "force". Default value: "force" |

Response body on success:


```
{
  "image_id": "<UUID>",
  "subject": "<face_name>"
}
```

| Element  | Type   | Description                |
| -------- | ------ | -------------------------- |
| image_id | UUID   | UUID of uploaded image     |
| subject  | string | <face_name> of saved image |



### **Recognize a face**

Recognizes faces from the uploaded images.
```http request
curl  -X POST "http://localhost:8000/api/v1/recognize" \
-H "Content-Type: multipart/form-data" \
-H "x-api-key: <model_api_key>" \
-F file=<local_file>
-F limit=<limit>
-F prediction_count=<prediction_count>
```


| Element          | Description | Type    | Required | Notes                                                        |
| ---------------- | ----------- | ------- | -------- | ------------------------------------------------------------ |
| Content-Type     | header      | string  | required | multipart/form-data                                          |
| x-api-key        | header      | string  | required | api key of the model, created by the user                    |
| file             | body        | image   | required | allowed image formats: jpeg, jpg, ico, png, bmp, gif, tif, tiff, webp. Max size is 5Mb |
| limit            | body        | integer | optional | maximum number of faces to be recognized. Value of 0 represents no limit. Default value: 0 |
| prediction_count | body        | integer | optional | maximum number of predictions per faces. Default value: 1    |

Response body on success:
```
{
  "result": [
    {
      "box": {
        "probability": <probability>,
        "x_max": <integer>,
        "y_max": <integer>,
        "x_min": <integer>,
        "y_min": <integer>
      },
      "faces": [
        {
          "similarity": <similarity1>,
          "subject": <face_name1>	
        },
        ...
      ]
    }
  ]
}
```

| Element                        | Type    | Description                                                  |
| ------------------------------ | ------- | ------------------------------------------------------------ |
| box                            | object  | list of parameters of the bounding box for this face         |
| probability                    | float   | probability that a found face is actually a face             |
| x_max, y_max, x_min, y_min | integer | coordinates of the frame containing the face                 |
| faces                          | list    | list of similar faces with size of <prediction_count> order by similarity |
| similarity                     | float   | similarity that on that image predicted person              |
| subject                        | string  | name of the subject in model                                 |



### Get list of saved images

Retrieves a list of images saved in a model

```http request
curl  -X GET "http://localhost:8000/api/v1/faces" \
-H "x-api-key: <model_api_key>" \
```

| Element   | Description | Type   | Required | Notes                                     |
| --------- | ----------- | ------ | -------- | ----------------------------------------- |
| x-api-key | header      | string | required | api key of the model, created by the user |

Response body on success:

```
{
  "faces": [
    {
      "image_id": <face_id>,
      "subject": <face_name>
    },
    ...
  ]
}
```

| Element  | Type   | Description                                                  |
| -------- | ------ | ------------------------------------------------------------ |
| image_id | UUID   | UUID of the face                                             |
| subject  | string | <face_name> of the person, whose picture was saved for this api key |



### Delete examples of the face

Deletes all image examples of the <face_name>.

```http request
curl  -X DELETE "http://localhost:8000/api/v1/faces/?subject=<face_name>&retrain=<retrain_option>" \
-H "x-api-key: <model_api_key>"
```

| Element        | Description | Type   | Required | Notes                                                        |
| -------------- | ----------- | ------ | -------- | ------------------------------------------------------------ |
| x-api-key      | header      | string | required | api key of the model, created by the user                    |
| face_name      | param       | string | optional | is the name you assign to the image you save. **Caution!** If this parameter is absent, all faces in model will be removed |
| retrain_option | param       | string | optional | retrains the model after deleting. Can be set to "yes", "no", "force". Default value: "force" |
Response body on success:
```
[
  {
    "image_id": <face_id>,
    "subject": <face_name>
  },
  ...
]
```

| Element  | Type   | Description                                                  |
| -------- | ------ | ------------------------------------------------------------ |
| image_id | UUID   | UUID of the removed face                                     |
| subject  | string | <face_name> of the person, whose picture was saved for this api key |



### Delete examples of the face by ID

Deletes image by ID.

```http request
curl  -X DELETE "http://localhost:8000/api/v1/faces/<image_id>retrain=<retrain_option>" \
-H "x-api-key: <model_api_key>"
```

| Element        | Description | Type   | Required | Notes                                                        |
| -------------- | ----------- | ------ | -------- | ------------------------------------------------------------ |
| x-api-key      | header      | string | required | api key of the model, created by the user                    |
| image_id       | variable    | UUID   | required | UUID of the removing face                                    |
| retrain_option | param       | string | optional | retrains the model after deleting. Can be set to "yes", "no", "force". Default value: "force" |
Response body on success:
```
{
  "image_id": <face_id>,
  "subject": <face_name>
}
```

| Element  | Type   | Description                                                  |
| -------- | ------ | ------------------------------------------------------------ |
| image_id | UUID   | UUID of the removed face                                     |
| subject  | string | <face_name> of the person, whose picture was saved for this api key |



## Contributing

Contributions are welcomed and greatly appreciated.

After creating your first contributing PR you will be requested to sign our 
Contributor License Agreement by commenting your PR with a
special message.



### Formatting standards

For java just import dev/team_codestyle.xml file in your IntelliJ IDEA



### Report Bugs

Report bugs at https://github.com/exadel-inc/face-recognition-service/issues.

If you are reporting a bug, please include:

* Your operating system name and version.
* Any details about your local setup that might be helpful in troubleshooting.
* Detailed steps to reproduce the bug.



### Submit Feedback

The best way to send feedback is to file an issue at https://github.com/exadel-inc/face-recognition-service/issues. 

If you are proposing a feature, please:

* Explain in detail how it should work.
* Keep the scope as narrow as possible, to make it easier to implement.



## License info

CompreFace is Open Source software released under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.html).

