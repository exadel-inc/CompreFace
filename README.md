# Exadel Face Recognition System (Exadel FRS)

[![GitHub license](https://img.shields.io/github/license/pospielov/frs-private)](https://www.apache.org/licenses/LICENSE-2.0) [![GitHub contributors](https://img.shields.io/github/contributors/pospielov/frs-private)](https://github.com/pospielov/frs-private/graphs/contributors)



 [Exadel Face Recognition System (Exadel FRS)](#exadel-face-recognition-system--exadel-frs-)
  * [Features](#features)
  * [Getting started](#getting-started)
  * [How it works](#how-it-works)
    + [ML Technologies](#ml-technologies)
    + [Used ML Papers and Algorithms](#used-ml-papers-and-algorithms)
  * [Technologies](#technologies)
    + [Architecture diagram](#architecture-diagram)
    + [Databases](#databases)
    + [Platform server](#platform-server)
    + [API server](#api-server)
    + [Embedding server](#embedding-server)
  * [Photo example of recognition](#photo-example-of-recognition)
  * [Simple tutorial of usage](#simple-tutorial-of-usage)
  * [Rest API description](#rest-api-description)
    + [Add an example of the face](#add-an-example-of-the-face)
    + [Recognize faces from given image](#recognize-faces-from-given-image)
    + [Retrain face model](#retrain-face-model)
    + [Retraining status](#retraining-status)
    + [Abort training](#abort-training)
    + [Delete all examples of the face by name](#delete-all-examples-of-the-face-by-name)
    + [List names of all saved faces](#list-names-of-all-saved-faces)
  * [Use cases and domains](#use-cases-and-domains)
  * [Contributing](#contributing)
  * [License info](#license-info)



It is the solution for face recognition that can be integrated as a standalone server. 

We combined state-of-the-art face recognition library that uses deep neural networks, trained on million faces, to retrieve features from faces with our own machine learning algorithm for face recognition. 

Every user could have several models trained on different subset of people. The key idea is to make solution for faces recognition that anybody could setup and use without machine learning knowledge.



## Features

FRS is:

- just one docker command for app start
- fast and high accuracy face recognizing
- runs fully on-prem (control your data)
- UI application panel for management
- highload support



## Getting started

1. install Docker
1. just run command:
`
docker-compose up --build
`
1. open http://localhost:8000/



## How it works



![how-it-works](https://user-images.githubusercontent.com/4942439/87042598-4ff45f00-c20d-11ea-8142-7aba30723117.jpg)

- **Finding the face:** we reused base project code for this, for face recognition they use multi-task cascaded convolutional neural
 networks (MTCNN).
- **Posing and projecting faces:** we reused base project code for this.
- **Calculate embedding from faces:** we reused base project code for this, they take pretrained CNN for face recognition, remove the last
 3 fully connected layers and the result NN calculates embedding.
- **Use embedding for training model/recognize face using embedding:**
right now we use haifengl/smile [LogisticRegression](http://haifengl.github.io/api/java/smile/classification/LogisticRegression.html) as a classifier, because of small number of train examples.



### ML Technologies

* MTCNN (Multi-task Cascaded Convolutional Networks)
* logistic Regression
* transfer learning


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



### Databases

* postgreSQL
* mongoDB



### Platform server

* java 11
* spring boot

  

### API server

* java 11
* spring boot
* haifengl/smile



### Embedding server

* python
* [faceNet](https://github.com/davidsandberg/facenet)
* [insightFace](https://github.com/deepinsight/insightface)
* tensorFlow
* sciPy
* numPy
* openCV (for images resizing)



## Photo example of recognition

<todo: add photo> 



## Simple tutorial of usage

1. What is needed to make it work is just create model and receive API key.
1. Then will be able to use this API key to train model with your own images.
1. As far as you trained the model you just need to send new face to REST endpoint to recognize face on it.



## Rest API description

<todo: swagger link>

### Add an example of the face

Add an example image of a know person's face. It will be used during face recognition. Face will be recognized after the model is trained.
```http request
curl  -X POST \
-H "Content-Type: multipart/form-data" \
-H "x-frs-api-key: <model_api_key>" \
-F file=<localfilename>
-F det_prob_threshold=<det_prob_threshold>
-F retrain=<retrain_option>

http://localhost:8000/api/v1/faces/<face_name>
```
- **[model_api_key]** - api key of model, created by client, to which application has access (in core service it is equal to "X-Api-Key
" header)
- **[localfilename]** - jpeg of png image on your local computer
- **[face_name]** - name of the person on the image. It could be any string if you need depersonalize images
- **[det_prob_threshold]** (optional) - the minimum required confidence that a found face is actually a face. Value between 0.0 and 1.0
- **[retrain_option]** - (optional) specify whether the model should start retraining immediately after the request is completed (set this
 parameter to
 value "no", if operating with a lot of images one after another). Allowed values: "yes", "no", "force". “Force” option will abort already running
 processes of classifier training. Default value: *FORCE*

**Available images extensions:** jpeg, jpg, ico, png, bmp, gif, tif, tiff, webp

**Max images size:** 5Mb (5242880 bytes)



### Recognize faces from given image

Recognizes faces from given image.
```http request
curl  -X POST \
-H "Content-Type: multipart/form-data" \
-H "x-frs-api-key: <model_api_key>" \
-F file=<local_file>
-F limit=<limit>
-F prediction_count=<prediction_count>

https://localhost:8000/api/v1/recognize
```

- **[model_api_key]** - api key of model, created by client, to which application has access (in core service it is equal to "X-Api-Key
" header)
- **[local_file]** - path to jpeg of png image on your local computer
- **[limit]** (optional) - parameter to specify the maximum number of faces to be recognized. Value of 0 represents no limit. Default
 value: 0
- **[prediction_count]** (optional) - parameter to specify the maximum number of predictions per faces. Default value: 1

**Available images extensions:** jpeg, jpg, ico, png, bmp, gif, tif, tiff, webp

**Max images size:** 5Mb (5242880 bytes)

Response body on success:
```
{
  "result": [
    {
      "box": {
        "probability": <det_probability>,
        "x_max": <integer>,
        "y_max": <integer>,
        "x_min": <integer>,
        "y_min": <integer>
      },
      "faces": [
        {
          "similarity": <similarity1>,
          "face_name": <face_name1>
        },
        ...
      ]
    }
  ]
}
```

- **[box]** - list of parameters of the bounding box for this face
    - **[det_probability]** - probability that a found face is actually a face
- **[faces]** - list of similar faces with size of **[prediction_count]** order by similarity
    - **[face_name]** - name of the person in model
    - **[similarity]** - similarity that on that image predicted person



### Retrain face model

Retrains model for specified API Key. The model should be retrained if updates to the face database were done with the retrain  flag to  false (which is useful when uploading/deleting a lot of faces). 

```http request
curl  -X POST \
-H "x-frs-api-key: <model_api_key>" \

https://localhost:8000/api/v1/retrain
```

- **[model_api_key]** - api key of model, created by client, to which application has access (in core service it is equal to "X-Api-Key" header)

Response body on success:
```json
{
  "status": "Training is started"
}
```



### Retraining status

Gets face model retraining status for specified API Key. This REST endpoint could be useful if you want to check if the model is still retraining before adding more faces and retraining model again.

```http request
curl  -X GET \
-H "x-frs-api-key: <model_api_key>" \

https://localhost:8000/api/v1/retrain
```

- **[model_api_key]** - api key of model, created by client, to which application has access (in core service it is equal to "X-Api-Key
" header)

Response body on success:
```json
{
  "status": "Retraining has been previously started"
}
```
or

```json
{
  "status": "Ready to start training"
}
```



### Abort training

Aborts training the specified API Key. This REST endpoint could be useful if you want to stop retraining the model.

```http request
curl  -X DELETE \
-H "x-frs-api-key: <model_api_key>" \

https://localhost:8000/api/v1/retrain
```

- **[model_api_key]** - api key of model , created by client , to which application has access (in core service it is equal to "X-Api-Key
" header)

Response body on success:
```json
{
  "status": "Retraining is ensured to be stopped"
}
```


### Delete all examples of the face by name

Delete all image examples of the face. Face will not be recognized after the model is retrained.

```http request
curl  -X DELETE \
-H "x-frs-api-key: <model_api_key>"
-F retrain=<retrain_option>

https://localhost:8000/api/v1/faces/<face_name>
```

- **[model_api_key]** - api key of model, created by client, to which application has access (in core service it is equal to "X-Api-Key
" header)
- **[face_name]** - the name of the person, whose records need to be removed from the database, as a string
- **[retrain_option]** - (optional) specify whether the model should start retraining immediately after the request is completed (set this
 parameter to
 value "no", if operating with a lot of images one after another). Allowed values: "yes", "no", "force". “Force” option will abort
  already running processes of classifier training. Default value: *FORCE*



### List names of all saved faces

As with all other endpoints, applies only to faces uploaded with the specified API key.

```http request
curl  -X GET \
-H "x-frs-api-key: <model_api_key>" \
https://localhost:8000/api/v1/faces
```

- **[model_api_key]** - api key of model , created by client , to which application has access (in core service it is equal to "X-Api-Key
" header)

Response body on success:
```
{
  "faces": [
    {
      "id": <face_id>,
      "name": <face_name>
    },
    ...
  ]
}
```

- **[face_id]** - id of the face in the database
- **[face_name]** - name of the person, whose picture was saved for this api key 



### Delete model and all faces that belong to it

Delete model and everything related to it from MongoDB. Please, note that it has no effect on PostgreSQL data. So deleting model from MongoDB doesn't remove model's record from PostgreSQL. Originally, endpoint was created to reffer from [admin](/admin) modul.

```http request
curl  -X DELETE \
-H "x-frs-api-key: <model_api_key>"
https://localhost:8000/api/v1/faces
```

- **[model_api_key]** - api key of model, created by client, to which application has access (in core service it is equal to "X-Api-Key
" header)

## Use cases and domains

<todo: cases and domains>



## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.
You can help us with opened issues too. There's always something to work on.
We have rules on formatting:

- for java just import team_codestyle.xml file in your IntelliJ IDEA
<todo: links to code style files> 



## License info

[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)