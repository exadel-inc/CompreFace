# CompreFace

[![GitHub license](https://img.shields.io/github/license/exadel-inc/CompreFace)](https://www.apache.org/licenses/LICENSE-2.0) [![GitHub contributors](https://img.shields.io/github/contributors/exadel-inc/CompreFace)](https://github.com/exadel-inc/CompreFace/graphs/contributors)



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
    + [List of all saved faces](#list-of-all-saved-faces)
    + [Delete all examples of the face by name](#delete-all-examples-of-the-face-by-name)
    + [Delete an example of the face by ID](#delete-an-example-of-the-face-by-id)
    + [Verify faces from given image](#verify-faces-from-given-image)
  * [Contributing](#contributing)
    + [Formatting standards](#formatting-standards)
    + [Report Bugs](#report-bugs)
    + [Submit Feedback](#submit-feedback)
  * [License info](#license)



## Overview

CompreFace is an application for facial recognition that can be integrated as a standalone server or deployed on the cloud and can be set up and used without machine learning expertise.
Our method is based on deep neural networks, which is one of the most popular facial recognition methods and provides a convenient API for model training and face recognition. We also provide an easy-to-understand roles system with which you can easily control who has access to the model.
Every user can create their own models and train them on different subsets of input data. 



## Features

The system can accurately identify people even when it is only given one example of their face.
CompreFace:
 - Uses open-source code and operates fully on-premises for data security 
 - Can be set up and used without machine learning expertise
 - Uses one of the most popular face recognition methods for highest accuracy 
 - Includes a UI panel with roles for access control
 - Starts quickly with one docker command



## Getting started

To get started:
1. Install Docker
2. Download the archive from our latest release: https://github.com/exadel-inc/CompreFace/releases
3. Unzip the archive
4. Run Docker
5. Windows search bar-> cmd->in the Command prompt-> cd ->paste the path to the extracted zip folder
6. Run command: _docker-compose up --build_
7. Open http://localhost:8000/

Getting started for Contributors:

1. Install Docker 
2. Clone repository
3. Open dev folder
4. Run command: _docker-compose up --build_
5. Open http://localhost:8000/

** Tips for Windows** (use Git Bash terminal)

1. Turn of the git autocrlf with command: _git config --global core.autocrlf false_
2. Make sure all your containers are down: _$ docker ps_
3. In case some containers are working, they should be stopped: _$ docker-compose down_
4. Clean all local datebases and images: _docker system prune --volumes_
5. Last line in /dev/start.sh file change to _docker-compose -f docker-compose.yml up --remove-orphans --build_
6. Go to Dev folder cd dev
7. Run _sh start.sh_ and make sure http://localhost:8000/ starts
8. Stopped all containers: $ docker-compose down
9. Run _sh start--dev.sh_ and make sure http://localhost:4200/ starts


## Simple tutorial of usage

1. Registration users in the app
1. Creating applications, Face Collections, inviting users
1. Integrating your app via API if need
1. Images uploading, filling a Face Collection with your own images by using the API key
1. Send a new image to recognize the face on it.

![how-it-works](https://user-images.githubusercontent.com/4942439/92221961-b3baa180-eeb7-11ea-89c9-af2bec2295fc.png)



## How it works

**Finding a face** 

Detecting one or more faces in an image. Multi-task Cascaded Convolutional Neural Networks (MTCNN) was used for face recognition.

**Posing and projecting faces** 

Normalization of all found faces with rotate, scale and shear. 

**Calculate embedding from faces**

Calculating embedding and classifying the face based on extracted features. We took CNN for face recognition and the last 3 fully connected layers were removed. As a result, - NN calculates embedding. 

**Use embedding for recognize/verify faces using embedding**

Recognizing the person in the photo. We calculate Euclidean distance using [Nd4j](https://javadoc.io/static/org.nd4j/nd4j-api/0.4-rc3.6/org/nd4j/linalg/factory/Nd4j.html) to determine the level of matching faces.



### ML technologies

* [MTCNN (Multi-task Cascaded Convolutional Networks)](https://arxiv.org/pdf/1604.02878.pdf)
* [FaceNet](https://github.com/davidsandberg/facenet)
* Euclidean distance



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



![architecture](https://user-images.githubusercontent.com/59657282/96593288-00d0c680-12f2-11eb-93ae-584a2cd08a3d.png)



### Database

* PostgreSQL



### Platform server

* Java 11
* Spring Boot

  

### API server

* Java 11
* Spring Boot
* Nd4j



### Embedding server

* Python
* [FaceNet](https://github.com/davidsandberg/facenet)
* [InsightFace](https://github.com/deepinsight/insightface)
* TensorFlow
* SciPy
* NumPy
* OpenCV (for images resizing)



## Rest API description

By using the created API key, the user can add an image as an example of the face, retrieve a list of saved images, recognize a face from the uploaded image the Face Collection, and delete all examples of the face by the name.



### Add an example of the face

Creates an example of the face by saving images. To train the system, you can add as many images as you want.

```http request
curl  -X POST "http://localhost:8000/api/v1/faces?subject=<subject>&det_prob_threshold=<det_prob_threshold>" \
-H "Content-Type: multipart/form-data" \
-H "x-api-key: <faces_collection_api_key>" \
-F file=@<local_file> 
```
| Element             | Description | Type   | Required | Notes                                                        |
| ------------------- | ----------- | ------ | -------- | ------------------------------------------------------------ |
| Content-Type        | header      | string | required | multipart/form-data                                          |
| x-api-key           | header      | string | required | api key of the Face Collection, created by the user          |
| subject             | param       | string | required | is the name you assign to the image you save                 |
| det_prob_ threshold | param       | string | optional | minimum required confidence that a recognized face is actually a face. Value is between 0.0 and 1.0 |
| file                | body        | image  | required | allowed image formats: jpeg, jpg, ico, png, bmp, gif, tif, tiff, webp. Max size is 5Mb |

Response body on success:
```
{
  "image_id": "<UUID>",
  "subject": "<subject>"
}
```

| Element  | Type   | Description                |
| -------- | ------ | -------------------------- |
| image_id | UUID   | UUID of uploaded image     |
| subject  | string | <subject> of saved image |



### Recognize faces from given image

Recognizes faces from the uploaded image.
```http request
curl  -X POST "http://localhost:8000/api/v1/recognize?limit=<limit>&prediction_count=<prediction_count>" \
-H "Content-Type: multipart/form-data" \
-H "x-api-key: <faces_collection_api_key>" \
-F file=<local_file>
```


| Element          | Description | Type    | Required | Notes                                                        |
| ---------------- | ----------- | ------- | -------- | ------------------------------------------------------------ |
| Content-Type     | header      | string  | required | multipart/form-data                                          |
| x-api-key        | header      | string  | required | api key of the Face Collection, created by the user                    |
| file             | body        | image   | required | allowed image formats: jpeg, jpg, ico, png, bmp, gif, tif, tiff, webp. Max size is 5Mb |
| limit            | param       | integer | optional | maximum number of faces with best similarity in result. Value of 0 represents no limit. Default value: 0 |
| prediction_count | param       | integer | optional | maximum number of predictions per faces. Default value: 1    |

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
          "subject": <subject1>	
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
| subject                        | string  | name of the subject in Face Collection                                 |



### List of all saved faces

Retrieves a list of images saved in a Face Collection

```http request
curl  -X GET "http://localhost:8000/api/v1/faces" \
-H "x-api-key: <faces_collection_api_key>" \
```

| Element   | Description | Type   | Required | Notes                                     |
| --------- | ----------- | ------ | -------- | ----------------------------------------- |
| x-api-key | header      | string | required | api key of the Face Collection, created by the user |

Response body on success:

```
{
  "faces": [
    {
      "image_id": <image_id>,
      "subject": <subject>
    },
    ...
  ]
}
```

| Element  | Type   | Description                                                  |
| -------- | ------ | ------------------------------------------------------------ |
| image_id | UUID   | UUID of the face                                             |
| subject  | string | <subject> of the person, whose picture was saved for this api key |



### Delete all examples of the face by name

Deletes all image examples of the <subject>.

```http request
curl  -X DELETE "http://localhost:8000/api/v1/faces?subject=<subject>" \
-H "x-api-key: <faces_collection_api_key>"
```

| Element   | Description | Type   | Required | Notes                                                        |
| --------- | ----------- | ------ | -------- | ------------------------------------------------------------ |
| x-api-key | header      | string | required | api key of the Face Collection, created by the user                    |
| subject   | param       | string | optional | is the name you assign to the image you save. **Caution!** If this parameter is absent, all faces in Face Collection will be removed |

Response body on success:
```
[
  {
    "image_id": <image_id>,
    "subject": <subject>
  },
  ...
]
```

| Element  | Type   | Description                                                  |
| -------- | ------ | ------------------------------------------------------------ |
| image_id | UUID   | UUID of the removed face                                     |
| subject  | string | <subject> of the person, whose picture was saved for this api key |



### Delete an example of the face by ID

Deletes an image by ID.

```http request
curl  -X DELETE "http://localhost:8000/api/v1/faces/<image_id>" \
-H "x-api-key: <faces_collection_api_key>"
```

| Element   | Description | Type   | Required | Notes                                     |
| --------- | ----------- | ------ | -------- | ----------------------------------------- |
| x-api-key | header      | string | required | api key of the Face Collection, created by the user |
| image_id  | variable    | UUID   | required | UUID of the removing face                 |

Response body on success:
```
{
  "image_id": <image_id>,
  "subject": <subject>
}
```

| Element  | Type   | Description                                                  |
| -------- | ------ | ------------------------------------------------------------ |
| image_id | UUID   | UUID of the removed face                                     |
| subject  | string | <subject> of the person, whose picture was saved for this api key |



### Verify faces from given image

Compares faces from the uploaded image with face in saved image id.
```http request
curl  -X POST "http://localhost:8000/api/v1/faces/<image_id>/verify?limit=<limit>" \
-H "Content-Type: multipart/form-data" \
-H "x-api-key: <faces_collection_api_key>" \
-F file=<local_file>
```


| Element          | Description | Type    | Required | Notes                                                        |
| ---------------- | ----------- | ------- | -------- | ------------------------------------------------------------ |
| Content-Type     | header      | string  | required | multipart/form-data                                          |
| x-api-key        | header      | string  | required | api key of the Face Collection, created by the user                    |
| image_id         | variable    | UUID    | required | UUID of the verifying face                                   |
| file             | body        | image   | required | allowed image formats: jpeg, jpg, ico, png, bmp, gif, tif, tiff, webp. Max size is 5Mb |
| limit            | param       | integer | optional | maximum number of faces with best similarity in result. Value of 0 represents no limit. Default value: 0 |

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
      "similarity": <similarity1>
    },
    ...
  ]
}
```

| Element                        | Type    | Description                                                  |
| ------------------------------ | ------- | ------------------------------------------------------------ |
| box                            | object  | list of parameters of the bounding box for this face         |
| probability                    | float   | probability that a found face is actually a face             |
| x_max, y_max, x_min, y_min     | integer | coordinates of the frame containing the face                 |
| similarity                     | float   | similarity that on that image predicted person               |
| subject                        | string  | name of the subject in Face Collection                       |



## Contributing

Contributions are welcome and greatly appreciated.
After creating your first contributing Pull Request you will receive a request to sign our Contributor License Agreement by commenting your PR with a special message.




### Formatting standards

For java just import dev/team_codestyle.xml file in your IntelliJ IDEA



### Report Bugs

Report bugs at https://github.com/exadel-inc/CompreFace/issues.

If you are reporting a bug, please include:

* Your operating system name and version.
* Any details about your local setup that might be helpful in troubleshooting.
* Detailed steps to reproduce the bug.



### Submit Feedback

The best way to send feedback is to file an issue at https://github.com/exadel-inc/CompreFace/issues. 

If you are proposing a feature, please:

* Explain in detail how it should work.
* Keep the scope as narrow as possible, to make it easier to implement.



## License info

CompreFace is Open Source software released under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.html).

