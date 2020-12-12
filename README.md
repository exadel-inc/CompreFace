
<h1 align="center">CompreFace - open-source face recognition system from Exadel</h1>

<p align="center">
    <a target="_blank" href="https://exadel.com/services/engineering/ai-machine-learning/compreface/">
  <img src="https://user-images.githubusercontent.com/3736126/101276437-6e0ebd00-37b5-11eb-9df8-6bc2bb0f922d.png" alt="angular-logo" height="250px"/>
 </a>
  <br>
  <i>CompreFace is a free face recognition service that can be easily integrated into<br> any system without prior machine learning skills.
     </i>
  <br>
</p>

<p align="center">
  <a href="https://exadel.com/services/engineering/ai-machine-learning/compreface/"><strong>Official website</strong></a>
  <br>
</p>

<p align="center">
  <a href="#contributing">Contributing</a>
  ·
  <a href="https://github.com/exadel-inc/CompreFace/issues">Submit an Issue</a>
  ·
  <a href="https://exadel.com/news/tag/compreface/">Blog</a>
  ·
  <a href="https://gitter.im/CompreFace/community">Community chat</a>
  <br>
</p>

<p align="center">
  <a href="https://www.apache.org/licenses/LICENSE-2.0">
    <img src="https://img.shields.io/github/license/exadel-inc/CompreFace" alt="GitHub license" />
  </a>&nbsp;
  <a href="https://github.com/exadel-inc/CompreFace/graphs/contributors">
    <img src="https://img.shields.io/github/contributors/exadel-inc/CompreFace" alt="GitHub contributors" />
  </a>&nbsp;
</p>
<hr>

# CompreFace

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
    + [Admin server](#admin-server)
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

CompreFace is docker-based application for facial recognition that can be integrated as a standalone server or deployed on the cloud and can be set up and used without machine learning expertise.
Our method is based on deep neural networks, which is one of the most popular facial recognition methods and provides a convenient REST API for Face Collection training and face recognition. We also provide a roles system with which you can easily control who has access to the Face Collection.
Every user can create their own models and train them on different subsets of input data. 

## Feedback survey

We are constantly improving our product. But for better understanding which features we should add or improve we need your help!
Feedback form is totally anonymous, it will take just 2 minutes of your time to answer the questions:
https://forms.gle/ybAEPc3XmzEcpv4M8

## Features

The system can accurately identify people even when it is only given one example of their face.
CompreFace:
 - Uses open-source code and operates fully on-premises for data security 
 - Can be set up and used without machine learning expertise
 - Uses one of the most popular face recognition methods for highest accuracy 
 - Includes a UI panel with roles for access control
 - Starts quickly with one docker command


## Getting started

#### To get started (Linux, MacOS):
1. Install Docker and Docker-Compose
2. Download the archive from our latest release: https://github.com/exadel-inc/CompreFace/releases
3. Unzip the archive
4. Run command: _docker-compose up -d_
5. Open in your browser: http://localhost:8000/login

#### Getting started for Contributors:

1. Install Docker and Docker-Compose
2. Clone repository
3. Open dev folder
4. Run command: _docker-compose up --build_
5. Open in your browser: http://localhost:8000/login

#### To get started (Windows):
1. Install Docker
2. Download the archive from our latest release: https://github.com/exadel-inc/CompreFace/releases
3. Unzip the archive
4. Run Docker
5. Windows search bar-> cmd->in the Command prompt-> cd ->paste the path to the extracted zip folder
6. Run command: _docker-compose up -d_
7. Open http://localhost:8000/login

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

Step 1. You need to sign up to the system (First user in CompareFace admin has Owner role, but it is possible to change the role) and then LogIn with created account or just use the existing one. After that system redirects you to the main page.

Step 2. Create an application (left section) with "Create" link at the bottom of the page. An application is where you can create and manage your face collections.

Step 3. Enter you application with double click on the name of the application. Here you will have two possibilities. The first one is to add new users to your application and manage permissions ( Global Owner and Administrator roles already have access to any application without invite, user role doesn't.) The second one is to create face collections.

Step 4. After creating new collection, it appears at the Face Collections List created within the application with an appropriate name and API key. The user has the possibility to add new Face or to test the existing one (three dots on right side and click "test" link). This option will lead the user to Test Face Collection page, where is the drag&drop to upload image with face to recognize. We recommend an image size no higher than 5MB, as it could slow down the request process. Supported image formats are JPEG/PNG/JPG/ICO/BMP/GIF/TIF/TIFF format.

Step 5. Upload your photo and let Face Recognition system compare faces. When you have face contour detection enabled (green borders around the face). These points represent the shape of the feature. API requests within the solution use RESTful API, and backend data collection. [Read more about API](https://github.com/exadel-inc/CompreFace#rest-api-description) With Face Recognition system APIs you can add Face Recognition capabilities using simple API Calls.

The following result Json illustrates how these points map to a face, where

1. subject -person identificator
2. similarity - gives the confidence that this is the found subject
3. probability - gives the confidence that this is a face
4. x_min, x_max, y_min, y_max are coordinates of the face in the image

```

"result": [
{
  "box": {
    "probability": 0.99583,
    "x_max": 551,
    "y_max": 364,
    "x_min": 319,
    "y_min": 55
  },
  "faces": [
  {
    "similarity": 0.99593,
    "face_name": "lisan"
  }
  ]
}
]
```

The following JavaScript code example allows to add new face to Face Collection.

```

 async function saveNewImageToFaceCollection() {
  let name = encodeURIComponent('John');
  let formData = new FormData();
  let photo = document.getElementById("fileDropRef").files[0];

    formData.append("photo", photo);

    try {
       let r = await fetch('http://localhost:8000/api/v1/faces/?subject=`${name}`', {method: "POST", body: formData});
     } catch(e) {
       console.log('Houston, we have a problem...:', e);
    }

 }

```

This function sends image to our server and shows result in text area:

```

function recognizeFace(input) {

  async function getData() {
    let response = await fetch('http://localhost:8000/api/v1/recognize')
    let data = await response.json()
    return data
  };

  let result = Promise.resolve(response)
    result.then(data => {
    document.getElementById("result-textarea-request").innerHTML = JSON.stringify(data);
  });
}

```

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



![architecture](https://user-images.githubusercontent.com/3736126/101276552-71ef0f00-37b6-11eb-8b30-4455d09e74cc.png)



### Database

* PostgreSQL



### Admin server

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
| det_prob_ threshold | param       | string | optional | minimum required confidence that a recognized face is actually a face. Value is between 0.0 and 1.0. |
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
curl  -X POST "http://localhost:8000/api/v1/faces/recognize?limit=<limit>&prediction_count=<prediction_count>&det_prob_threshold=<det_prob_threshold>" \
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
| det_prob_ threshold | param       | string | optional | minimum required confidence that a recognized face is actually a face. Value is between 0.0 and 1.0. |
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
curl  -X POST "http://localhost:8000/api/v1/faces/<image_id>/verify?limit=<limit>&det_prob_threshold=<det_prob_threshold>" \
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
| det_prob_ threshold | param       | string | optional | minimum required confidence that a recognized face is actually a face. Value is between 0.0 and 1.0. |

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

