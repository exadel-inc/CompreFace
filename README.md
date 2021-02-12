
<h1 align="center">CompreFace: An Open-Source Face Recognition Solution by Exadel</h1>

<p align="center">
    <a target="_blank" href="https://exadel.com/services/engineering/ai-machine-learning/compreface/">
  <img src="https://user-images.githubusercontent.com/3736126/101276437-6e0ebd00-37b5-11eb-9df8-6bc2bb0f922d.png" alt="compreface-logo" height="250px"/>
 </a>
  <br>
  <i>CompreFace is a free face recognition solution that can be easily integrated into<br> any IT system without prior machine learning skills
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

  * [Overview](#overview)
  * [Screenshots](#screenshots)
  * [Feedback survey](#feedback-survey)
  * [Features](#features)
  * [Getting Started with CompreFace](#getting-started-with-compreface)
  * [How to Use CompreFace](#how-to-use-compreface)
  * [Rest API description](#rest-api-description)
    + [Add an Example of a Subject](#add-an-example-of-a-subject)
    + [Recognize Faces from a Given Image](#recognize-faces-from-a-given-image)
    + [List of All Saved Subjects](#list-of-all-saved-subjects)
    + [Delete All Examples of the Subject by Name](#delete-all-examples-of-the-subject-by-name)
    + [Delete an Example of the Subject by ID](#delete-an-example-of-the-subject-by-id)
    + [Verify Faces from a Given Image](#verify-faces-from-a-given-image)
 * [User Roles System](#user-roles-system)
    + [Global Roles](#global-roles)
    + [Application Roles](#application-roles)
 * [How CompreFace Works Under the Hood](#how-compreface-works-under-the-hood)
    + [ML technologies](#ml-technologies)
    + [Used ML Papers and Algorithms](#used-ml-papers-and-algorithms)
 * [Technologies](#technologies)
    + [Architecture diagram](#architecture-diagram)
    + [Database](#database)
    + [Admin server](#admin-server)
    + [API server](#api-server)
    + [Embedding server](#embedding-server)
  * [Contributing](#contributing)
    + [Formatting standards](#formatting-standards)
    + [Report Bugs](#report-bugs)
    + [Submit Feedback](#submit-feedback)
  * [License info](#license)


## Overview

CompreFace is a face detection and recognition GitHub project. Essentially, it is a docker-based application that can be used as a standalone server or deployed in the cloud. You don’t need prior machine learning skills to set up and use CompreFace.

Our approach to face detection and recognition is based on FaceNet and InsightFace libraries that use deep neural networks. CompreFace provides a convenient REST API for training algorithms to detect and recognize faces from your collection of images (aka Face Collection). The solution also features a role management system that allows you to easily control who has access to your Face Collection.

## Screenshots
<p align="center">
<img src="https://user-images.githubusercontent.com/3736126/107061938-6a151080-67e1-11eb-95ba-c4dd43471f5b.png" alt="compreface-test-page" width=390px style="padding: 10px;">
<img src="https://user-images.githubusercontent.com/3736126/107063429-0f7cb400-67e3-11eb-9ecc-27a1a0955923.png" alt="compreface-main-page" width=390px style="padding: 10px;">
</p>

## Feedback survey

We need your help to better understand which features we should add to the service and how we can improve it further! Our feedback form is totally anonymous, and answering the questions will take just 2 minutes of your time: 
https://forms.gle/ybAEPc3XmzEcpv4M8

## Features

The system can accurately identify people even when it has only “seen” their photo once. Technology-wise, CompreFace has several advantages over similar free face recognition solutions:
CompreFace:
- The service is open-source and self-hosted, which gives you additional guaranties for data security
- CompreFace can be deployed either in the cloud or on premises
- Our facial recognition service can be set up and used without machine learning expertise
- The solution uses FaceNet and InsightFace libraries, which are based on deep neural networks
- CompreFace features a UI panel for convenient user roles and access management
- The service starts quickly with just one docker command

## Getting Started with CompreFace

#### To get started (Linux, MacOS):

1. Install Docker and Docker Compose
2. Download the archive from our latest release: https://github.com/exadel-inc/CompreFace/releases
3. Unzip the archive
4. Open the terminal in this folder and run this command: `docker-compose up -d`
5. Open the service in your browser: http://localhost:8000/login

#### To get started (Windows):
1. Install Docker Desktop
2. Download the archive from our latest release: https://github.com/exadel-inc/CompreFace/releases
3. Unzip the archive
4. Run Docker
5. Open Command prompt (write `cmd` in windows search bar)
6. Open folder where you extracted zip archive (Write `cd path_of_the_folder`, press enter).
7. Run command: `docker-compose up -d`
8. Open http://localhost:8000/login

#### Getting started for contributors:

1. Install Docker and Docker Compose
2. Clone the repository
3. Open the dev folder
4. Open the terminal in this folder and run this command: `docker-compose up --build`
5. Open CompreFace in your browser: http://localhost:8000/login

#### Tips for contributors

1. Turn off the git autocrlf with the following command: `git config --global core.autocrlf false`
2. Make sure all your containers are down: `docker ps`
3. If some containers are working, they should be stopped: `docker-compose down`
4. Clean all local databases and images: `docker system prune --volumes`
5. Change the last line in the `/dev/start.sh` file to `docker-compose -f docker-compose.yml up --remove-orphans --build`
6. Go to the Dev folder: `cd dev`
7. Run sh start.sh and make sure http://localhost:8000/ starts
8. To stop all containers: `docker-compose stop`
9. Run `sh start--dev.sh` and make sure http://localhost:4200/ starts


## How to Use CompreFace

**Step 1.** You need to sign up for the system and log in into the account you’ve just created or use the one you already have. After that, the system redirects you to the main page.

**Step 2.** Create an application (left section) using the "Create" link at the bottom of the page. An application is where you can create and manage your Face Collections.

**Step 3.** Enter your application by clicking on its name. Here you will have two options: you can either add new users and manage their access roles or create new Face Collections.

**Step 4.** After creating a new collection, you will see it in the Face Collections List with an appropriate name and API key. To add known faces to your Face Collection, you can use REST API. Once you’ve uploaded all known faces, you can test the collection using REST API or the TEST page. We recommend that you use an image size no higher than 5MB, as it could slow down the request process. The supported image formats include JPEG/PNG/JPG/ICO/BMP/GIF/TIF/TIFF.

**Step 5.** Upload your photo and let our open-source face recognition system match the image against the Face Collection. If you use a UI for face recognition, you will see the original picture with marks near every face. If you use REST API, you will receive a response in JSON format.

JSON contains an array of objects that represent each recognized face. Each object has the following fields:

1. `subject` - person identificator
2. `similarity` - gives the confidence that this is the found subject
3. `probability` - gives the confidence that this is a face
4. `x_min`, `x_max`, `y_min`, `y_max` are coordinates of the face in the image


```json
{
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
}
```

Here is a JavaScript code snippet that loads a new image to your Face Collection:

```js
async function saveNewImageToFaceCollection() {
    let name = encodeURIComponent('John');
    let formData = new FormData();
    let photo = document.getElementById("fileDropRef").files[0];

    formData.append("photo", photo);

    try {
        let r = await fetch('http://localhost:8000/api/v1/faces/?subject=`${name}`', {method: "POST", body: formData});
    } catch (e) {
        console.log('Houston, we have a problem...:', e);
    }
}
```

This function sends the image to our server and shows results in a text area:

```js
function recognizeFace(input) {

    async function getData() {
        let response = await fetch('http://localhost:8000/api/v1/recognize')
        let data = await response.json()
        return data
    }

    let result = Promise.resolve(response)
    result.then(data => {
        document.getElementById("result-textarea-request").innerHTML = JSON.stringify(data);
    });
}
```


## Rest API description

By using the created API key, the user can add an image as an example of a particular face, retrieve a list of saved images, recognize a face from the image uploaded to the Face Collection, and delete all examples of the face by the name.


### Add an Example of a Subject

This creates an example of the subject by saving images. You can add as many images as you want to train the system.

```shell
curl -X POST "http://localhost:8000/api/v1/faces?subject=<subject>&det_prob_threshold=<det_prob_threshold>" \
-H "Content-Type: multipart/form-data" \
-H "x-api-key: <faces_collection_api_key>" \
-F file=@<local_file> 
```
| Element             | Description | Type   | Required | Notes                                                        |
| ------------------- | ----------- | ------ | -------- | ------------------------------------------------------------ |
| Content-Type        | header      | string | required | multipart/form-data                                          |
| x-api-key           | header      | string | required | api key of the Face Collection, created by the user          |
| subject             | param       | string | required | is the name you assign to the image you save                 |
| det_prob_threshold  | param       | string | optional | minimum required confidence that a recognized face is actually a face. Value is between 0.0 and 1.0. |
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



### Recognize Faces from a Given Image

Recognizes faces from the uploaded image.
```shell
curl -X POST "http://localhost:8000/api/v1/faces/recognize?limit=<limit>&prediction_count=<prediction_count>&det_prob_threshold=<det_prob_threshold>" \
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



### List of All Saved Subjects

To retrieve a list of subjects saved in a Face Collection:

```shell
curl -X GET "http://localhost:8000/api/v1/faces" \
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


### Delete All Examples of the Subject by Name

To delete all image examples of the <subject>:

```shell
curl -X DELETE "http://localhost:8000/api/v1/faces?subject=<subject>" \
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



### Delete an Example of the Subject by ID

To delete an image by ID:

```shell
curl -X DELETE "http://localhost:8000/api/v1/faces/<image_id>" \
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



### Verify Faces from a Given Image

To compare faces from the uploaded images with the face in saved image ID:
```shell
curl -X POST "http://localhost:8000/api/v1/faces/<image_id>/verify?limit=<limit>&det_prob_threshold=<det_prob_threshold>" \
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

## User Roles System

Compreface roles system consists of two types of roles - global roles and application roles. The users with these roles have different responsibilities, so we recommend that you delimit such users and follow our recommendations to avoid giving too much access to sensitive information. Of course in small teams and at your own risk you can ignore these recommendations.

### Global Roles

Global roles define what permissions you have in the system itself, and the main responsibility of such users is to maintain the system itself. We recommend that the most permissive (owner and administrator) roles be added to technical support employees. Then there is no reason to add such users to applications as they still will have all permissions within the application.

In CompreFace, the first user automatically receives the global owner role and has rights for any operation within CompreFace - managing users, and creating and managing applications. The only restriction for the global owner is that such a user can’t delete themselves from the system, so the user will have to assign the global owner role to somebody else and then remove themselves from the system.

Users with the global administrator role have the same permissions as users with the global owner role. The only difference is that such users can’t manage the user with the global owner role. We recommend reducing users with such a role to the minimum number required to maintain the system.  

All new users are automatically assigned the global user role. These users can’t create applications, can access only the applications to which they were added, and can’t manage other users. These users use CompreFace for face recognition and are part of the development team; they are not responsible for managing other users and their permissions.

### Application Roles

Application roles define the role of the user within an application, and the main responsibility of such users is to develop applications into which they are going to integrate Compreface. We recommend that the most permissive roles (owner and administrator) be added as project managers and team leads, as they are responsible for the application. We also recommend that all application users have the global user role. To become a member of an application team, users with a global user role need to be added to the application directly by the global owner, global administrator, or application owner.

The user that creates an application automatically receives the application owner role and has rights for any operation within the application - managing the application and its users, and creating and managing Face Collections. The only restriction for the application owner is that they can’t delete themselves from the application, so they have to assign the application owner role to somebody else before deleting themselves.

Users with the application administrator role (global user role + application admin role) can create and manage Face Collections but can’t manage an application and its users.

Users with the application user role can’t manage anything in the application. This is the least permissive role (global user role + application user role), but this provides enough information to integrate CompreFace with any other application, so we recommend that most CompreFace users have this role.


## How CompreFace Works Under the Hood

**Finding a face**

To detect one or more faces in an image, we used multi-task cascaded convolutional neural networks (MTCNNs).

**Posing and projecting faces**

To normalize all detected faces, we perform rotate, scale, and shear operations.

**Calculating embeddings from faces**

To calculate facial features, which are called embeddings, we use convolutional deep neural networks.

**Recognizing and verifying faces using embeddings**

To evaluate the level of similarity between saved faces and those in provided photos, we calculate the Euclidean distance using the 
[ND4J library](https://javadoc.io/static/org.nd4j/nd4j-api/0.4-rc3.6/org/nd4j/linalg/factory/Nd4j.html) to determine the level of matching faces.


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



## Contributing

We want to improve our open-source face recognition solution, so your contributions are welcome and greatly appreciated. After creating your first contributing pull request, you will receive a request to sign our Contributor License Agreement by commenting your pull request with a special message.


### Formatting standards

For java just import dev/team_codestyle.xml file in your IntelliJ IDEA.


### Report Bugs

Please report any bugs here://github.com/exadel-inc/CompreFace/issues.

If you are reporting a bug, please specify:

- Your operating system name and version
- Any details about your local setup that might be helpful in troubleshooting
- Detailed steps to reproduce the bug


### Submit Feedback

The best way to send us feedback is to file an issue at https://github.com/exadel-inc/CompreFace/issues. 

If you are proposing a feature, please:

- Explain in detail how it should work.
- Keep the scope as narrow as possible to make it easier to implement.


## License info

CompreFace is open-source real-time facial recognition software released under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.html).

