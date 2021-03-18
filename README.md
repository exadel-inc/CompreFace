
<h1 align="center">CompreFace is a free and open-source face recognition system from Exadel</h1>

<p align="center">
    <a target="_blank" href="https://exadel.com/solutions/compreface/">
  <img src="https://user-images.githubusercontent.com/3736126/101276437-6e0ebd00-37b5-11eb-9df8-6bc2bb0f922d.png" alt="angular-logo" height="250px"/>
 </a>
  <br>
  <i>CompreFace can be easily integrated into any system without prior machine learning skills. CompreFace provides REST API for face 
recognition, face verification, face detection, landmark detection, age, and gender recognition and is easily deployed with docker
     </i>
  <br>
</p>

<p align="center">
  <a href="https://exadel.com/solutions/compreface/"><strong>Official website</strong></a>
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

# Table Of Contents

  * [Overview](#overview)
  * [Screenshots](#screenshots)
  * [Feedback survey](#feedback-survey)
  * [Features](#features)
  * [Getting Started with CompreFace](#getting-started-with-compreface)
  * [Documentation](/docs)
    * [How to Use CompreFace](/docs/How-to-Use-CompreFace.md)
    * [Face Services and Plugins](/docs/Face-services-and-plugins.md)
    * [Rest API Description](/docs/Rest-API-description.md)
    * [Configuration](/docs/Configuration.md)
    * [Architecture and Scalability](/docs/Architecture-and-scalability.md)
    * [Custom Builds](/docs/Custom-builds.md)
    * [User Roles System](/docs/User-Roles-System.md)
    * [Gathering Anonymous Statistics](/docs/Gathering-anonymous-statistics.md)
  * [Contributing](#contributing)
  * [License info](#license-info)


# Overview

CompreFace is a free and open-source face detection and recognition GitHub project. Essentially, it is a docker-based application that can be used as a standalone server or deployed in the cloud. You don’t need prior machine learning skills to set up and use CompreFace.

CompreFace provides REST API for face recognition, face verification, face detection, landmark detection, age, and gender recognition. The solution also features a role management system that allows you to easily control who has access to your Face Recognition Services.

CompreFace is delivered as a docker-compose config and supports different models that work on CPU and GPU. Our solution is based on state-of-the-art methods and libraries like FaceNet and InsightFace.

# Screenshots
<p align="center">
<img src="https://user-images.githubusercontent.com/3736126/107061938-6a151080-67e1-11eb-95ba-c4dd43471f5b.png" alt="compreface-test-page" width=390px style="padding: 10px;">
<img src="https://user-images.githubusercontent.com/3736126/107063429-0f7cb400-67e3-11eb-9ecc-27a1a0955923.png" alt="compreface-main-page" width=390px style="padding: 10px;">
</p>

# Feedback survey

We need your help to better understand which features we should add to the service and how we can improve it further! Our feedback form is totally anonymous, and answering the questions will take just 2 minutes of your time:
https://forms.gle/ybAEPc3XmzEcpv4M8

# Features

The system can accurately identify people even when it has only “seen” their photo once. Technology-wise, CompreFace has several advantages over similar free face recognition solutions. CompreFace:

- Supports many face recognition services: face identification, face verification, face detection, landmark detection, and age and 
gender recognition
- Supports both CPU and GPU and is easy to scale up
- Is open source and self-hosted, which gives you additional guarantees for data security
- Can be deployed either in the cloud or on premises
- Can be set up and used without machine learning expertise
- Uses FaceNet and InsightFace libraries, which use state-of-the-art face recognition methods
- Features a UI panel for convenient user roles and access management
- Starts quickly with just one docker command


# Getting Started with CompreFace

### To get started (Linux, MacOS):

1. Install Docker and Docker Compose
2. Download the archive from our latest release: https://github.com/exadel-inc/CompreFace/releases
3. Unzip the archive
4. Open the terminal in this folder and run this command: `docker-compose up -d`
5. Open the service in your browser: http://localhost:8000/login

### To get started (Windows):

1. Install Docker Desktop
2. Download the archive from our latest release: https://github.com/exadel-inc/CompreFace/releases
3. Unzip the archive
4. Run Docker
5. Open Command prompt (write `cmd` in windows search bar)
6. Open folder where you extracted zip archive (Write `cd path_of_the_folder`, press enter).
7. Run command: `docker-compose up -d`
8. Open http://localhost:8000/login

## Getting started for devs

Follow this link: https://github.com/exadel-inc/CompreFace/tree/develop/dev

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
       let r = await fetch('http://localhost:8000/api/v1/recognition/faces/?subject=`${name}`', {method: "POST", body: formData});
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

### To get started for Developers: 

More documentation is available [here](/dev/README.md)

# Documentation

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
curl  -X POST "http://localhost:8000/api/v1/recognition/faces?subject=<subject>&det_prob_threshold=<det_prob_threshold>" \
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
curl  -X POST "http://localhost:8000/api/v1/recognition/recognize?limit=<limit>&prediction_count=<prediction_count>&det_prob_threshold=<det_prob_threshold>&face_plugins=<face_plugins>&status=<status>" \
-H "Content-Type: multipart/form-data" \
-H "x-api-key: <faces_collection_api_key>" \
-F file=<local_file>
```


| Element             | Description | Type    | Required | Notes                                                        |
| ------------------- | ----------- | ------- | -------- | ------------------------------------------------------------ |
| Content-Type        | header      | string  | required | multipart/form-data                                          |
| x-api-key           | header      | string  | required | api key of the Face Collection, created by the user                    |
| file                | body        | image   | required | allowed image formats: jpeg, jpg, ico, png, bmp, gif, tif, tiff, webp. Max size is 5Mb |
| limit               | param       | integer | optional | maximum number of faces with best similarity in result. Value of 0 represents no limit. Default value: 0 |
| det_prob_ threshold | param       | string  | optional | minimum required confidence that a recognized face is actually a face. Value is between 0.0 and 1.0. |
| prediction_count    | param       | integer | optional | maximum number of predictions per faces. Default value: 1    |
| face_plugins        | param       | string  | optional | comma-separated slugs of face plugins. Empty value - face plugins disabled, returns only bounding boxes. E.g. calculator,gender,age - returns embedding, gender and age for each face.    |
| status              | param       | boolean | optional | special parameter to show execution_time and plugin_version fields. Empty or false value - both fields eliminated, true - both fields included |

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
      "landmarks": [
        [144,158],
        [218,159],
        [182,185],
        [154,229],
        [207,228]
      ],
      "age": [
          25,
          32
      ],
      "gender": "male",
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
| x_max, y_max, x_min, y_min     | integer | coordinates of the frame containing the face                 |
| faces                          | list    | list of similar faces with size of <prediction_count> order by similarity |
| similarity                     | float   | similarity that on that image predicted person               |
| subject                        | string  | name of the subject in Face Collection                       |
| landmarks                      | list    | list of the coordinates of the frame containing the face-landmarks |



### List of all saved faces

Retrieves a list of images saved in a Face Collection

```http request
curl  -X GET "http://localhost:8000/api/v1/recognition/faces" \
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
curl  -X DELETE "http://localhost:8000/api/v1/recognition/faces?subject=<subject>" \
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
curl  -X DELETE "http://localhost:8000/api/v1/recognition/faces/<image_id>" \
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
curl  -X POST "http://localhost:8000/api/v1/recognition/faces/<image_id>/verify?limit=<limit>&det_prob_threshold=<det_prob_threshold>&face_plugins=<face_plugins>&status=<status>" \
-H "Content-Type: multipart/form-data" \
-H "x-api-key: <faces_collection_api_key>" \
-F file=<local_file>
```


| Element            | Description | Type    | Required | Notes                                                        |
| ------------------ | ----------- | ------- | -------- | ------------------------------------------------------------ |
| Content-Type       | header      | string  | required | multipart/form-data                                          |
| x-api-key          | header      | string  | required | api key of the Face Collection, created by the user                    |
| image_id           | variable    | UUID    | required | UUID of the verifying face                                   |
| file               | body        | image   | required | allowed image formats: jpeg, jpg, ico, png, bmp, gif, tif, tiff, webp. Max size is 5Mb |
| limit              | param       | integer | optional | maximum number of faces with best similarity in result. Value of 0 represents no limit. Default value: 0 |
| det_prob_threshold | param       | string  | optional | minimum required confidence that a recognized face is actually a face. Value is between 0.0 and 1.0. |
| face_plugins       | param       | string  | optional | comma-separated slugs of face plugins. Empty value - face plugins disabled, returns only bounding boxes. E.g. calculator,gender,age - returns embedding, gender and age for each face.    |
| status             | param       | boolean | optional | Special parameter to show execution_time and plugin_version fields. Empty or false value - both fields eliminated, true - both fields included |

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



### Verify faces on two images

Compare faces from given two images:
* processFile - file to be verified
* checkFile - reference file to check the processed file
```http request
curl  -X POST "http://localhost:8000/api/v1/verification/verify?limit=<limit>&det_prob_threshold=<det_prob_threshold>&face_plugins=<face_plugins>&status=<status>" \
-H "Content-Type: multipart/form-data" \
-H "x-api-key: <faces_collection_api_key>" \
-F checkFile=<local_check_file>
-F processFile=<local_process_file>
```


| Element             | Description | Type    | Required | Notes                                                        |
| ------------------- | ----------- | ------- | -------- | ------------------------------------------------------------ |
| Content-Type        | header      | string  | required | multipart/form-data                                          |
| x-api-key           | header      | string  | required | api key of the Face Collection, created by the user                    |
| image_id            | variable    | UUID    | required | UUID of the verifying face                                   |
| processFile         | body        | image   | required | allowed image formats: jpeg, jpg, ico, png, bmp, gif, tif, tiff, webp. Max size is 5Mb |
| checkFile           | body        | image   | required | allowed image formats: jpeg, jpg, ico, png, bmp, gif, tif, tiff, webp. Max size is 5Mb |
| limit               | param       | integer | optional | maximum number of faces with best similarity in result. Value of 0 represents no limit. Default value: 0 |
| det_prob_ threshold | param       | string  | optional | minimum required confidence that a recognized face is actually a face. Value is between 0.0 and 1.0. |
| face_plugins        | param       | string  | optional | comma-separated slugs of face plugins. Empty value - face plugins disabled, returns only bounding boxes. E.g. calculator,gender,age - returns embedding, gender and age for each face.    |
| status              | param       | boolean | optional | Special parameter to show execution_time and plugin_version fields. Empty or false value - both fields eliminated, true - both fields included |

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
| box                            | object  | list of parameters of the bounding box for this face (on processedImage) |
| probability                    | float   | probability that a found face is actually a face (on processedImage)     |
| x_max, y_max, x_min, y_min     | integer | coordinates of the frame containing the face (on processedImage)         |
| similarity                     | float   | similarity between faces on given images                     |



## Contributing

Contributions are welcome and greatly appreciated.
After creating your first contributing Pull Request you will receive a request to sign our Contributor License Agreement by commenting your PR with a special message.

More documentation is available [here](/docs)

# Contributing

We want to improve our open-source face recognition solution, so your contributions are welcome and greatly appreciated. After creating your first contributing pull request, you will receive a request to sign our Contributor License Agreement by commenting your pull request with a special message.

### Formatting standards

For java just import dev/team_codestyle.xml file in your IntelliJ IDEA.

### Report Bugs

Please report any bugs [here](https://github.com/exadel-inc/CompreFace/issues).

If you are reporting a bug, please specify:

- Your operating system name and version
- Any details about your local setup that might be helpful in troubleshooting
- Detailed steps to reproduce the bug


### Submit Feedback

The best way to send us feedback is to file an issue at https://github.com/exadel-inc/CompreFace/issues.

If you are proposing a feature, please:

- Explain in detail how it should work.
- Keep the scope as narrow as possible to make it easier to implement.


# License info

CompreFace is open-source real-time facial recognition software released under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.html).

