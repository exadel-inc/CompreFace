# Rest API description

## Table Of Contents

+ [Postman REST API documentation](#postman-documentation)
+ [Face Recognition Service](#face-recognition-service)
  + [Managing Subjects](#managing-subjects)
      + [Add a Subject](#add-a-subject)
      + [Rename a Subject](#rename-a-subject)
      + [Delete a Subject](#delete-a-subject)
      + [Delete All Subjects](#delete-all-subjects)
      + [List Subjects](#list-subjects)
  + [Managing Subject Examples](#managing-subject-examples)
    + [Add an Example of a Subject](#add-an-example-of-a-subject)
    + [List of All Saved Examples of the Subject](#list-of-all-saved-examples-of-the-subject)
    + [Delete All Examples of the Subject by Name](#delete-all-examples-of-the-subject-by-name)
    + [Delete an Example of the Subject by ID](#delete-an-example-of-the-subject-by-id)
    + [Delete Multiple Examples](#delete-multiple-examples)
    + [Direct Download an Image example of the Subject by ID](#direct-download-an-image-example-of-the-subject-by-id)
    + [Download an Image example of the Subject by ID](#download-an-image-example-of-the-subject-by-id)
  + [Recognize Faces from a Given Image](#recognize-faces-from-a-given-image)
  + [Verify Faces from a Given Image](#verify-faces-from-a-given-image)
+ [Face Detection Service](#face-detection-service)
+ [Face Verification Service](#face-verification-service)
+ [Base64 Support](#base64-support)
+ [Recognition and verification using embedding](#recognition-and-verification-using-embedding)

To know more about face services and face plugins visit [this page](Face-services-and-plugins.md).

## Postman documentation

There is a [Postman REST API documentation](https://documenter.getpostman.com/view/17578263/UUxzAnde)
that covers the same REST endpoint. Postman documentation supports snippets on the most popular programming languages.

## Face Recognition Service

### Managing Subjects

These endpoints allow you to work with subjects.

The most popular case of subject usage is to assign a subject to one person. 
So, to upload several images of one person, you need to upload them to one subject. 
As a result, when you perform face recognition, you find a person who is on the image.

Another case of subject usage is assigning a photo of several people as a subject. 
In this case, you need to detect all faces on the image and then save them to one subject. 
As a result, when you perform face recognition, you find all photos on which there is the person who is on the image.
You don’t need to work with subjects explicitly. 
You can just upload a new example of the subject and the subject will be created automatically. 
Or if you delete all the examples of the subject, it will be deleted automatically.

### Add a Subject
```since 0.6 version```

Create a new subject in Face Collection. Creating a subject is an optional step, 
you can [upload an example](#add-an-example-of-a-subject) without an existing subject, and a subject will be created automatically.

```shell
curl -X POST "http://localhost:8000/api/v1/recognition/subjects" \
-H "Content-Type: application/json" \
-H "x-api-key: <service_api_key>" \
-d '{"subject": "<subject_name>"}'
```

| Element      | Description | Type   | Required | Notes                                                                         |
|--------------|-------------|--------|----------|-------------------------------------------------------------------------------|
| Content-Type | header      | string | required | application/json                                                              |
| x-api-key    | header      | string | required | api key of the Face recognition service, created by the user                  |
| subject      | body param  | string | required | is the name of the subject. It can be a person name, but it can be any string |

Response body on success:
```json
{
  "subject": "<subject_name>"
}
```

| Element | Type   | Description                |
|---------|--------|----------------------------|
| subject | string | is the name of the subject |

### Rename a Subject
```since 0.6 version```

Rename existing subject. If a new subject name already exists, 
subjects are merged - all faces from the old subject name are **reassigned** to the subject with the new name, old subject removed.  

```shell
curl -X PUT "http://localhost:8000/api/v1/recognition/subjects/<subject>" \
-H "Content-Type: application/json" \
-H "x-api-key: <service_api_key>" \
-d '{"subject: <subject_name>"}'
```
| Element      | Description | Type   | Required | Notes                                                                         |
|--------------|-------------|--------|----------|-------------------------------------------------------------------------------|
| Content-Type | header      | string | required | application/json                                                              |
| x-api-key    | header      | string | required | api key of the Face recognition service, created by the user                  |
| subject      | body param  | string | required | is the name of the subject. It can be a person name, but it can be any string |

Response body on success:
```json
{
  "updated": "true|false"
}
```

| Element | Type    | Description       |
|---------|---------|-------------------|
| updated | boolean | failed or success |

### Delete a Subject
```since 0.6 version```

Delete existing subject and all saved faces.

```shell
curl -X DELETE "http://localhost:8000/api/v1/recognition/subjects/<subject>" \
-H "Content-Type: application/json" \
-H "x-api-key: <service_api_key>"
```
| Element      | Description | Type   | Required | Notes                                                                         |
|--------------|-------------|--------|----------|-------------------------------------------------------------------------------|
| Content-Type | header      | string | required | application/json                                                              |
| x-api-key    | header      | string | required | api key of the Face recognition service, created by the user                  |
| subject      | body param  | string | required | is the name of the subject. It can be a person name, but it can be any string |

Response body on success:
```json
{
  "subject": "<subject_name>"
}
```

| Element | Type   | Description                |
|---------|--------|----------------------------|
| subject | string | is the name of the subject |

### Delete All Subjects
```since 0.6 version```

Delete all existing subjects and all saved faces.

```shell
curl -X DELETE "http://localhost:8000/api/v1/recognition/subjects" \
-H "Content-Type: application/json" \
-H "x-api-key: <service_api_key>"
```
| Element      | Description | Type   | Required | Notes                                                        |
|--------------|-------------|--------|----------|--------------------------------------------------------------|
| Content-Type | header      | string | required | application/json                                             |
| x-api-key    | header      | string | required | api key of the Face recognition service, created by the user |

Response body on success:
```json
{
  "deleted": "<count>"
}
```

| Element | Type    | Description                |
|---------|---------|----------------------------|
| deleted | integer | number of deleted subjects |

### List Subjects
```since 0.6 version```

This returns all subject related to Face Collection.  

```shell
curl -X GET "http://localhost:8000/api/v1/recognition/subjects/" \
-H "Content-Type: application/json" \
-H "x-api-key: <service_api_key>"
```
| Element      | Description | Type   | Required | Notes                                                        |
|--------------|-------------|--------|----------|--------------------------------------------------------------|
| Content-Type | header      | string | required | application/json                                             |
| x-api-key    | header      | string | required | api key of the Face recognition service, created by the user |

Response body on success:
```json
{
  "subjects": [
    "<subject_name1>",
    "<subject_name2>"
    ]
}
```

| Element  | Type  | Description                             |
|----------|-------|-----------------------------------------|
| subjects | array | the list of subjects in Face Collection |

### Managing Subject Examples

The subject example is basically an image of a known face that you want to save to face collection. 

When you save a subject example, CompreFace calculates the embedding of the face (faceprint) and saves it into the database. 
By default, the image itself is also saved, it is needed for managing images, e.g. [download of the image](#direct-download-an-image-example-of-the-subject-by-id). You can change it using `save_images_to_db` parameter in [configuration](Configuration.md). 

One subject example is enough for face recognition, the accuracy will be high enough. But if you add more examples, the accuracy may be even better. 

### Add an Example of a Subject

This creates an example of the subject by saving images. You can add as many images as you want to train the system. Image should 
contain only one face.

```shell
curl -X POST "http://localhost:8000/api/v1/recognition/faces?subject=<subject>&det_prob_threshold=<det_prob_threshold>" \
-H "Content-Type: multipart/form-data" \
-H "x-api-key: <service_api_key>" \
-F file=@<local_file> 
```
| Element            | Description | Type   | Required | Notes                                                                                                |
|--------------------|-------------|--------|----------|------------------------------------------------------------------------------------------------------|
| Content-Type       | header      | string | required | multipart/form-data                                                                                  |
| x-api-key          | header      | string | required | api key of the Face recognition service, created by the user                                         |
| subject            | param       | string | required | is the name you assign to the image you save                                                         |
| det_prob_threshold | param       | string | optional | minimum required confidence that a recognized face is actually a face. Value is between 0.0 and 1.0. |
| file               | body        | image  | required | allowed image formats: jpeg, jpg, ico, png, bmp, gif, tif, tiff, webp. Max size is 5Mb               |

Response body on success:  
```json
{
  "image_id": "6b135f5b-a365-4522-b1f1-4c9ac2dd0728",
  "subject": "subject1"
}
```

| Element  | Type   | Description                |
|----------|--------|----------------------------|
| image_id | UUID   | UUID of uploaded image     |
| subject  | string | Subject of the saved image |


### List of All Saved Examples of the Subject

To retrieve a list of subjects saved in a Face Collection:

```shell
curl -X GET "http://localhost:8000/api/v1/recognition/faces?page=<page>&size=<size>&subject=<subject>" \
-H "x-api-key: <service_api_key>" \
```

| Element   | Description | Type    | Required | Notes                                                                                                      |
|-----------|-------------|---------|----------|------------------------------------------------------------------------------------------------------------|
| x-api-key | header      | string  | required | api key of the Face recognition service, created by the user                                               |
| page      | param       | integer | optional | page number of examples to return. Can be used for pagination. Default value is 0. Since 0.6 version       |
| size      | param       | integer | optional | faces on page (page size). Can be used for pagination. Default value is 20. Since 0.6 version              |
| subject   | param       | string  | optional | what subject examples endpoint should return. If empty, return examples for all subjects. Since 1.0 version|

Response body on success:

```
{
  "faces": [
    {
      "image_id": <image_id>,
      "subject": <subject>
    },
    ...
  ],
  "page_number": 0,
  "page_size": 10,
  "total_pages": 2,
  "total_elements": 12
}
```

| Element        | Type    | Description                                                       |
|----------------|---------|-------------------------------------------------------------------|
| face.image_id  | UUID    | UUID of the face                                                  |
| faсe.subject   | string  | <subject> of the person, whose picture was saved for this api key |
| page_number    | integer | page number                                                       |
| page_size      | integer | **requested** page size                                           |
| total_pages    | integer | total pages                                                       |
| total_elements | integer | total faces                                                       |


### Delete All Examples of the Subject by Name

To delete all image examples of the <subject>:

```shell
curl -X DELETE "http://localhost:8000/api/v1/recognition/faces?subject=<subject>" \
-H "x-api-key: <service_api_key>"
```

| Element   | Description | Type   | Required | Notes                                                                                          |
|-----------|-------------|--------|----------|------------------------------------------------------------------------------------------------|
| x-api-key | header      | string | required | api key of the Face recognition service, created by the user                                   |
| subject   | param       | string | optional | is the name subject. If this parameter is absent, all faces in Face Collection will be removed |

Response body on success:
```
{
    "deleted": <count>
}
```

| Element | Type    | Description             |
|---------|---------|-------------------------|
| deleted | integer | Number of deleted faces |



### Delete an Example of the Subject by ID

Endpoint to delete an image by ID. If no image found by id - 404.

```shell
curl -X DELETE "http://localhost:8000/api/v1/recognition/faces/<image_id>" \
-H "x-api-key: <service_api_key>"
```

| Element   | Description | Type   | Required | Notes                                                        |
|-----------|-------------|--------|----------|--------------------------------------------------------------|
| x-api-key | header      | string | required | api key of the Face recognition service, created by the user |
| image_id  | variable    | UUID   | required | UUID of the removing face                                    |

Response body on success:
```
{
  "image_id": <image_id>,
  "subject": <subject>
}
```

| Element  | Type   | Description                                                       |
|----------|--------|-------------------------------------------------------------------|
| image_id | UUID   | UUID of the removed face                                          |
| subject  | string | <subject> of the person, whose picture was saved for this api key |

  
### Delete Multiple Examples
  ```since 1.0 version```
  
To delete several subject examples:  
  ```shell
curl -X POST "http://localhost:8000/api/v1/recognition/faces/delete" \
-H "Content-Type: application/json" \
-H "x-api-key: <service_api_key>" \
-d '["<image_id1>","<image_id2>", ..., "<image_idN>"]'
```

| Element         | Description | Type   | Required | Notes                                                        |
|-----------------|-------------|--------|----------|--------------------------------------------------------------|
| service_api_key | header      | string | required | api key of the Face recognition service, created by the user |
| image_id        | variable    | UUID   | required | UUID of the removing face                                    |
  

  
Response body on success:
``` 
{
  "image_id": <image_id>,
  "subject": <subject>
}
``` 

| Element         | Description                                               | Type   | 
|-----------------|-----------------------------------------------------------|--------|
| image_id        | UUID of the removed face                                  | UUID   | 
| subject         | of the person, whose picture was saved for this api key   | string | 
  
If some image ids are not exists, they will be ignored
  

### Direct Download an Image example of the Subject by ID
```since 0.6 version```

You can paste this URL into the <img> html tag to show the image.

```shell
curl -X GET "http://localhost:8000/api/v1/static/<service_api_key>/images/<image_id>"
```

| Element         | Description | Type   | Required | Notes                                                        |
|-----------------|-------------|--------|----------|--------------------------------------------------------------|
| service_api_key | variable    | string | required | api key of the Face recognition service, created by the user |
| image_id        | variable    | UUID   | required | UUID of the image to download                                |

Response body is binary image. Empty bytes if image not found.


### Download an Image example of the Subject by ID
```since 0.6 version```

To download an image example of the Subject by ID:

```shell
curl -X GET "http://localhost:8000/api/v1/recognition/faces/<image_id>/img"
-H "x-api-key: <service_api_key>"
```

| Element   | Description | Type   | Required | Notes                                                        |
|-----------|-------------|--------|----------|--------------------------------------------------------------|
| x-api-key | header      | string | required | api key of the Face recognition service, created by the user |
| image_id  | variable    | UUID   | required | UUID of the image to download                                |

Response body is binary image. Empty bytes if image not found.


### Recognize Faces from a Given Image

To recognize faces from the uploaded image:

```shell
curl  -X POST "http://localhost:8000/api/v1/recognition/recognize?limit=<limit>&prediction_count=<prediction_count>&det_prob_threshold=<det_prob_threshold>&face_plugins=<face_plugins>&status=<status>&detect_faces=<detect_faces>" \
-H "Content-Type: multipart/form-data" \
-H "x-api-key: <service_api_key>" \
-F file=<local_file>
```

| Element            | Description | Type    | Required | Notes                                                                                                                                          |
|--------------------|-------------|---------|----------|------------------------------------------------------------------------------------------------------------------------------------------------|
| Content-Type       | header      | string  | required | multipart/form-data                                                                                                                            |
| x-api-key          | header      | string  | required | api key of the Face recognition service, created by the user                                                                                   |
| file               | body        | image   | required | allowed image formats: jpeg, jpg, ico, png, bmp, gif, tif, tiff, webp. Max size is 5Mb                                                         |
| limit              | param       | integer | optional | maximum number of faces on the image to be recognized. It recognizes the biggest faces first. Value of 0 represents no limit. Default value: 0 |
| det_prob_threshold | param       | string  | optional | minimum required confidence that a recognized face is actually a face. Value is between 0.0 and 1.0.                                           |
| prediction_count   | param       | integer | optional | maximum number of subject predictions per face. It returns the most similar subjects. Default value: 1                                         |
| face_plugins       | param       | string  | optional | comma-separated slugs of face plugins. If empty, no additional information is returned. [Learn more](Face-services-and-plugins.md)             |
| status             | param       | boolean | optional | if true includes system information like execution_time and plugin_version fields. Default value is false                                      |
| detect_faces       | param       | boolean | optional | if false, CompreFace won't run a face detector. Instead, it will treat the image as a cropped face. Default value is true. Since 1.2 version   |

Response body on success:
```json
{
  "result" : [ {
    "age" : {
      "probability": 0.9308982491493225,
      "high": 32,
      "low": 25
    },
    "gender" : {
      "probability": 0.9898611307144165,
      "value": "female"
    },
    "mask" : {
      "probability": 0.9999470710754395,
      "value": "without_mask"
    },
    "embedding" : [ 9.424854069948196E-4, "...", -0.011415496468544006 ],
    "box" : {
      "probability" : 1.0,
      "x_max" : 1420,
      "y_max" : 1368,
      "x_min" : 548,
      "y_min" : 295
    },
    "landmarks" : [ [ 814, 713 ], [ 1104, 829 ], [ 832, 937 ], [ 704, 1030 ], [ 1017, 1133 ] ],
    "subjects" : [ {
      "similarity" : 0.97858,
      "subject" : "subject1"
    } ],
    "execution_time" : {
      "age" : 28.0,
      "gender" : 26.0,
      "detector" : 117.0,
      "calculator" : 45.0,
      "mask": 36.0
    }
  } ],
  "plugins_versions" : {
    "age" : "agegender.AgeDetector",
    "gender" : "agegender.GenderDetector",
    "detector" : "facenet.FaceDetector",
    "calculator" : "facenet.Calculator",
    "mask": "facemask.MaskDetector"
  }
}
```

| Element                    | Type    | Description                                                                                                                                                 |
|----------------------------|---------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| age                        | object  | detected age range. Return only if [age plugin](Face-services-and-plugins.md#face-plugins) is enabled                                                       |
| gender                     | object  | detected gender. Return only if [gender plugin](Face-services-and-plugins.md#face-plugins) is enabled                                                       |
| pose                       | object  | detected head pose. Return only if [pose plugin](Face-services-and-plugins.md#face-plugins) is enabled                                                      |
| mask                       | object  | detected mask. Return only if [face mask plugin](Face-services-and-plugins.md#face-plugins) is enabled.                                                     |
| embedding                  | array   | face embeddings. Return only if [calculator plugin](Face-services-and-plugins.md#face-plugins) is enabled                                                   |
| box                        | object  | list of parameters of the bounding box for this face                                                                                                        |
| probability                | float   | probability that a found face is actually a face                                                                                                            |
| x_max, y_max, x_min, y_min | integer | coordinates of the frame containing the face                                                                                                                |
| landmarks                  | array   | list of the coordinates of the frame containing the face-landmarks. Return only if [landmarks plugin](Face-services-and-plugins.md#face-plugins) is enabled |
| subjects                   | list    | list of similar subjects with size of <prediction_count> order by similarity                                                                                |
| similarity                 | float   | similarity that on that image predicted person                                                                                                              |
| subject                    | string  | name of the subject in Face Collection                                                                                                                      |
| execution_time             | object  | execution time of all plugins                                                                                                                               |
| plugins_versions           | object  | contains information about plugin versions                                                                                                                  |


### Verify Faces from a Given Image

To compare faces from the uploaded images with the face in saved image ID:
```shell
curl -X POST "http://localhost:8000/api/v1/recognition/faces/<image_id>/verify?limit=<limit>&det_prob_threshold=<det_prob_threshold>&face_plugins=<face_plugins>&status=<status>" \
-H "Content-Type: multipart/form-data" \
-H "x-api-key: <service_api_key>" \
-F file=<local_file>
```


| Element            | Description | Type    | Required | Notes                                                                                                                                                 |
|--------------------|-------------|---------|----------|-------------------------------------------------------------------------------------------------------------------------------------------------------|
| Content-Type       | header      | string  | required | multipart/form-data                                                                                                                                   |
| x-api-key          | header      | string  | required | api key of the Face recognition service, created by the user                                                                                          |
| image_id           | variable    | UUID    | required | UUID of the verifying face                                                                                                                            |
| file               | body        | image   | required | allowed image formats: jpeg, jpg, ico, png, bmp, gif, tif, tiff, webp. Max size is 5Mb                                                                |
| limit              | param       | integer | optional | maximum number of faces on the target image to be recognized. It recognizes the biggest faces first. Value of 0 represents no limit. Default value: 0 |
| det_prob_threshold | param       | string  | optional | minimum required confidence that a recognized face is actually a face. Value is between 0.0 and 1.0.                                                  |
| face_plugins       | param       | string  | optional | comma-separated slugs of face plugins. If empty, no additional information is returned. [Learn more](Face-services-and-plugins.md)                    |
| status             | param       | boolean | optional | if true includes system information like execution_time and plugin_version fields. Default value is false                                             |

Response body on success:
```json
{
  "result": [
    {
      "age" : {
        "probability": 0.9308982491493225,
        "high": 32,
        "low": 25
      },
      "gender" : {
        "probability": 0.9898611307144165,
        "value": "female"
      },
      "mask" : {
        "probability": 0.9999470710754395,
        "value": "without_mask"
      },
      "embedding" : [ -0.049007344990968704, "...", -0.01753818802535534 ],
      "box" : {
        "probability" : 0.9997453093528748,
        "x_max" : 205,
        "y_max" : 167,
        "x_min" : 48,
        "y_min" : 0
      },
      "landmarks" : [ [ 260, 129 ], [ 273, 127 ], [ 258, 136 ], [ 257, 150 ], [ 269, 148 ] ],
      "similarity" : 0.97858,
      "execution_time" : {
        "age" : 59.0,
        "gender" : 30.0,
        "detector" : 177.0,
        "calculator" : 70.0,
        "mask": 36.0
      }
    }
  ],
  "plugins_versions" : {
    "age" : "agegender.AgeDetector",
    "gender" : "agegender.GenderDetector",
    "detector" : "facenet.FaceDetector",
    "calculator" : "facenet.Calculator",
    "mask": "facemask.MaskDetector"
  }
}
```

| Element                    | Type    | Description                                                                                                                                                 |
|----------------------------|---------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| age                        | object  | detected age range. Return only if [age plugin](Face-services-and-plugins.md#face-plugins) is enabled                                                       |
| gender                     | object  | detected gender. Return only if [gender plugin](Face-services-and-plugins.md#face-plugins) is enabled                                                       |
| pose                       | object  | detected head pose. Return only if [pose plugin](Face-services-and-plugins.md#face-plugins) is enabled                                                      |
| mask                       | object  | detected mask. Return only if [face mask plugin](Face-services-and-plugins.md#face-plugins) is enabled                                                      |
| embedding                  | array   | face embeddings. Return only if [calculator plugin](Face-services-and-plugins.md#face-plugins) is enabled                                                   |
| box                        | object  | list of parameters of the bounding box for this face                                                                                                        |
| probability                | float   | probability that a found face is actually a face                                                                                                            |
| x_max, y_max, x_min, y_min | integer | coordinates of the frame containing the face                                                                                                                |
| landmarks                  | array   | list of the coordinates of the frame containing the face-landmarks. Return only if [landmarks plugin](Face-services-and-plugins.md#face-plugins) is enabled |
| similarity                 | float   | similarity that on that image predicted person                                                                                                              |
| execution_time             | object  | execution time of all plugins                                                                                                                               |
| plugins_versions           | object  | contains information about plugin versions                                                                                                                  |

## Face Detection Service

To detect faces from the uploaded image:

```shell
curl  -X POST "http://localhost:8000/api/v1/detection/detect?limit=<limit>&det_prob_threshold=<det_prob_threshold>&face_plugins=<face_plugins>&status=<status>" \
-H "Content-Type: multipart/form-data" \
-H "x-api-key: <service_api_key>" \
-F file=<local_file>
```


| Element            | Description | Type    | Required | Notes                                                                                                                                          |
|--------------------|-------------|---------|----------|------------------------------------------------------------------------------------------------------------------------------------------------|
| Content-Type       | header      | string  | required | multipart/form-data                                                                                                                            |
| x-api-key          | header      | string  | required | api key of the Face Detection service, created by the user                                                                                     |
| file               | body        | image   | required | image where to detect faces. Allowed image formats: jpeg, jpg, ico, png, bmp, gif, tif, tiff, webp. Max size is 5Mb                            |
| limit              | param       | integer | optional | maximum number of faces on the image to be recognized. It recognizes the biggest faces first. Value of 0 represents no limit. Default value: 0 |
| det_prob_threshold | param       | string  | optional | minimum required confidence that a recognized face is actually a face. Value is between 0.0 and 1.0                                            |
| face_plugins       | param       | string  | optional | comma-separated slugs of face plugins. If empty, no additional information is returned. [Learn more](Face-services-and-plugins.md)             |
| status             | param       | boolean | optional | if true includes system information like execution_time and plugin_version fields. Default value is false                                      |

Response body on success:
```json
{
  "result" : [ {
    "age" : {
      "probability": 0.9308982491493225,
      "high": 32,
      "low": 25
    },
    "gender" : {
      "probability": 0.9898611307144165,
      "value": "female"
    },
    "mask" : {
      "probability": 0.9999470710754395,
      "value": "without_mask"
    },
    "embedding" : [ -0.03027934394776821, "...", -0.05117142200469971 ],
    "box" : {
      "probability" : 0.9987509250640869,
      "x_max" : 376,
      "y_max" : 479,
      "x_min" : 68,
      "y_min" : 77
    },
    "landmarks" : [ [ 156, 245 ], [ 277, 253 ], [ 202, 311 ], [ 148, 358 ], [ 274, 365 ] ],
    "execution_time" : {
      "age" : 30.0,
      "gender" : 26.0,
      "detector" : 130.0,
      "calculator" : 49.0,
      "mask": 36.0
    }
  } ],
  "plugins_versions" : {
    "age" : "agegender.AgeDetector",
    "gender" : "agegender.GenderDetector",
    "detector" : "facenet.FaceDetector",
    "calculator" : "facenet.Calculator",
    "mask": "facemask.MaskDetector"
  }
}
```

| Element                    | Type    | Description                                                                                                                                                 |
|----------------------------|---------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| age                        | object  | detected age range. Return only if [age plugin](Face-services-and-plugins.md#face-plugins) is enabled                                                       |
| gender                     | object  | detected gender. Return only if [gender plugin](Face-services-and-plugins.md#face-plugins) is enabled                                                       |
| pose                       | object  | detected head pose. Return only if [pose plugin](Face-services-and-plugins.md#face-plugins) is enabled                                                      |
| mask                       | object  | detected mask. Return only if [face mask plugin](Face-services-and-plugins.md#face-plugins) is enabled                                                      |
| embedding                  | array   | face embeddings. Return only if [calculator plugin](Face-services-and-plugins.md#face-plugins) is enabled                                                   |
| box                        | object  | list of parameters of the bounding box for this face (on processedImage)                                                                                    |
| probability                | float   | probability that a found face is actually a face (on processedImage)                                                                                        |
| x_max, y_max, x_min, y_min | integer | coordinates of the frame containing the face (on processedImage)                                                                                            |
| landmarks                  | array   | list of the coordinates of the frame containing the face-landmarks. Return only if [landmarks plugin](Face-services-and-plugins.md#face-plugins) is enabled |
| execution_time             | object  | execution time of all plugins                                                                                                                               |
| plugins_versions           | object  | contains information about plugin versions                                                                                                                  |


## Face Verification Service

To compare faces from given two images:
```shell
curl  -X POST "http://localhost:8000/api/v1/verification/verify?limit=<limit>&prediction_count=<prediction_count>&det_prob_threshold=<det_prob_threshold>&face_plugins=<face_plugins>&status=<status>" \
-H "Content-Type: multipart/form-data" \
-H "x-api-key: <service_api_key>" \
-F source_image=<local_check_file>
-F target_image=<local_process_file>
```


| Element            | Description | Type    | Required | Notes                                                                                                                                                 |
|--------------------|-------------|---------|----------|-------------------------------------------------------------------------------------------------------------------------------------------------------|
| Content-Type       | header      | string  | required | multipart/form-data                                                                                                                                   |
| x-api-key          | header      | string  | required | api key of the Face verification service, created by the user                                                                                         |
| source_image       | body        | image   | required | file to be verified. Allowed image formats: jpeg, jpg, ico, png, bmp, gif, tif, tiff, webp. Max size is 5Mb                                           |
| target_image       | body        | image   | required | reference file to check the source file. Allowed image formats: jpeg, jpg, ico, png, bmp, gif, tif, tiff, webp. Max size is 5Mb                       |
| limit              | param       | integer | optional | maximum number of faces on the target image to be recognized. It recognizes the biggest faces first. Value of 0 represents no limit. Default value: 0 |
| det_prob_threshold | param       | string  | optional | minimum required confidence that a recognized face is actually a face. Value is between 0.0 and 1.0.                                                  |
| face_plugins       | param       | string  | optional | comma-separated slugs of face plugins. If empty, no additional information is returned. [Learn more](Face-services-and-plugins.md)                    |
| status             | param       | boolean | optional | if true includes system information like execution_time and plugin_version fields. Default value is false                                             |

Response body on success:
```json
{
  "result" : [{
    "source_image_face" : {
      "age" : {
        "probability": 0.9308982491493225,
        "high": 32,
        "low": 25
      },
      "gender" : {
        "probability": 0.9898611307144165,
        "value": "female"
      },
      "mask" : {
        "probability": 0.9999470710754395,
        "value": "without_mask"
      },
      "embedding" : [ -0.0010271212086081505, "...", -0.008746841922402382 ],
      "box" : {
        "probability" : 0.9997453093528748,
        "x_max" : 205,
        "y_max" : 167,
        "x_min" : 48,
        "y_min" : 0
      },
      "landmarks" : [ [ 92, 44 ], [ 130, 68 ], [ 71, 76 ], [ 60, 104 ], [ 95, 125 ] ],
      "execution_time" : {
        "age" : 85.0,
        "gender" : 51.0,
        "detector" : 67.0,
        "calculator" : 116.0,
        "mask": 36.0
      }
    },
    "face_matches": [
      {
        "age" : {
          "probability": 0.9308982491493225,
          "high": 32,
          "low": 25
        },
        "gender" : {
          "probability": 0.9898611307144165,
          "value": "female"
        },
        "mask" : {
          "probability": 0.9999470710754395,
          "value": "without_mask"
        },
        "embedding" : [ -0.049007344990968704, "...", -0.01753818802535534 ],
        "box" : {
          "probability" : 0.99975,
          "x_max" : 308,
          "y_max" : 180,
          "x_min" : 235,
          "y_min" : 98
        },
        "landmarks" : [ [ 260, 129 ], [ 273, 127 ], [ 258, 136 ], [ 257, 150 ], [ 269, 148 ] ],
        "similarity" : 0.97858,
        "execution_time" : {
          "age" : 59.0,
          "gender" : 30.0,
          "detector" : 177.0,
          "calculator" : 70.0,
          "mask": 36.0
        }
      }],
    "plugins_versions" : {
      "age" : "agegender.AgeDetector",
      "gender" : "agegender.GenderDetector",
      "detector" : "facenet.FaceDetector",
      "calculator" : "facenet.Calculator",
      "mask": "facemask.MaskDetector"
    }
  }]
}
```

| Element                    | Type    | Description                                                                                                                                                 |
|----------------------------|---------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| source_image_face          | object  | additional info about source image face                                                                                                                     |
| face_matches               | array   | result of face verification                                                                                                                                 |
| age                        | object  | detected age range. Return only if [age plugin](Face-services-and-plugins.md#face-plugins) is enabled                                                       |
| gender                     | object  | detected gender. Return only if [gender plugin](Face-services-and-plugins.md#face-plugins) is enabled                                                       |
| pose                       | object  | detected head pose. Return only if [pose plugin](Face-services-and-plugins.md#face-plugins) is enabled                                                      |
| mask                       | object  | detected mask. Return only if [face mask plugin](Face-services-and-plugins.md#face-plugins) is enabled                                                      |
| embedding                  | array   | face embeddings. Return only if [calculator plugin](Face-services-and-plugins.md#face-plugins) is enabled                                                   |
| box                        | object  | list of parameters of the bounding box for this face                                                                                                        |
| probability                | float   | probability that a found face is actually a face                                                                                                            |
| x_max, y_max, x_min, y_min | integer | coordinates of the frame containing the face                                                                                                                |
| landmarks                  | array   | list of the coordinates of the frame containing the face-landmarks. Return only if [landmarks plugin](Face-services-and-plugins.md#face-plugins) is enabled |
| similarity                 | float   | similarity between this face and the face on the source image                                                                                               |
| execution_time             | object  | execution time of all plugins                                                                                                                               |
| plugins_versions           | object  | contains information about plugin versions                                                                                                                  |



## Base64 Support
`since 0.5.1 version`

Except `multipart/form-data`, all CompreFace endpoints, that require images as input, accept images in `Base64` format. 
The base rule is to use `Content-Type: application/json` header and send JSON in the body. 
The name of the JSON parameter coincides with the name of the `multipart/form-data` parameter.

### Add an Example of a Subject, Base64
Full description [here](#add-an-example-of-a-subject).

```shell
curl -X POST "http://localhost:8000/api/v1/recognition/faces?subject=<subject>&det_prob_threshold=<det_prob_threshold>" \
-H "Content-Type: application/json" \
-H "x-api-key: <service_api_key>" \
-d {"file": "<base64_value>"}
```

### Recognize Faces from a Given Image, Base64
Full description [here](#recognize-faces-from-a-given-image).

```shell
curl  -X POST "http://localhost:8000/api/v1/recognition/recognize?limit=<limit>&prediction_count=<prediction_count>&det_prob_threshold=<det_prob_threshold>&face_plugins=<face_plugins>&status=<status>" \
-H "Content-Type: application/json" \
-H "x-api-key: <service_api_key>" \
-d {"file": "<base64_value>"}
```

### Verify Faces from a Given Image, Base64
Full description [here](#verify-faces-from-a-given-image).

```shell
curl -X POST "http://localhost:8000/api/v1/recognition/faces/<image_id>/verify?
limit=<limit>&det_prob_threshold=<det_prob_threshold>&face_plugins=<face_plugins>&status=<status>" \
-H "Content-Type: application/json" \
-H "x-api-key: <service_api_key>" \
-d {"file": "<base64_value>"}
```

### Face Detection Service, Base64
Full description [here](#face-detection-service).

```shell
curl  -X POST "http://localhost:8000/api/v1/detection/detect?limit=<limit>&det_prob_threshold=<det_prob_threshold>&face_plugins=<face_plugins>&status=<status>" \
-H "Content-Type: application/json" \
-H "x-api-key: <service_api_key>" \
-d {"file": "<base64_value>"}
```

### Face Verification Service, Base64
Full description [here](#face-verification-service).

```shell
curl -X POST "http://localhost:8000/api/v1/verification/verify?limit=<limit>&prediction_count=<prediction_count>&det_prob_threshold=<det_prob_threshold>&face_plugins=<face_plugins>&status=<status>" \
-H "Content-Type: application/json" \
-H "x-api-key: <service_api_key>" \
-d {"source_image": "<source_image_base64_value>", "target_image": "<target_image_base64_value>"}
```



## Recognition and verification using embedding
`since 1.2.0 version`

You can use computed embedding to perform recognition and verification. To obtain embedding, you can
use [calculator plugin](https://github.com/exadel-inc/CompreFace/blob/EFRS-1333_ability_to_send_embeddings_instead_of_the_image_for_recognition/docs/Face-services-and-plugins.md#face-plugins)
in each Face service.
The base rule is to use `Content-Type: application/json` header and send JSON in the body.

### Recognize Faces from a Given Image, Embedding
The service is used to determine similarities between input embeddings and embeddings within the Face Collection. An example:

```shell
curl -X POST "http://localhost:8000/api/v1/recognition/embeddings/recognize?prediction_count=<prediction_count>" \
-H "Content-Type: application/json" \
-H "x-api-key: <service_api_key>" \
-d {"embeddings": [[<array_of_embedding>], ...]}
```

| Element          | Description | Type    | Required | Notes                                                                                                           |
|------------------|-------------|---------|----------|-----------------------------------------------------------------------------------------------------------------|
| Content-Type     | header      | string  | required | application/json                                                                                                |
| x-api-key        | header      | string  | required | an api key of the Face recognition service, created by the user                                                 |
| embeddings       | body        | array   | required | an input embeddings. The length depends on the model (e.g. 512 or 128)                                          |
| prediction_count | param       | integer | optional | the maximum number of subject predictions per embedding. It returns the most similar subjects. Default value: 1 |

Response body on success:
```json
{
  "result": [
    {
      "embedding": [0.0627421774604647, "...", -0.0236684433507126],
      "similarities": [
        {
          "subject": "John",
          "similarity": 0.55988
        },
        "..."
      ]
    },
    "..."
  ]
}
```

| Element      | Type   | Description                                                                                |
|--------------|--------|--------------------------------------------------------------------------------------------|
| result       | array  | an array that contains all the results                                                     |
| embedding    | array  | an input embedding                                                                         |
| similarities | array  | an array that contains results of similarity between the embedding and the input embedding |
| subject      | string | a subject in which the similar embedding was found                                         |
| similarity   | float  | a similarity between the embedding and the input embedding                                 |

### Verify Faces from a Given Image, Embedding
The endpoint is used to compare input embeddings to the embedding stored in Face Collection. An example:

```shell
curl -X POST "http://localhost:8000/api/v1/recognition/embeddings/faces/{image_id}/verify" \
-H "Content-Type: application/json" \
-H "x-api-key: <service_api_key>" \
-d {"embeddings": [[<array_of_embeddings>], ...]}
```

| Element      | Description | Type   | Required | Notes                                                                  |
|--------------|-------------|--------|----------|------------------------------------------------------------------------|
| Content-Type | header      | string | required | application/json                                                       |
| x-api-key    | header      | string | required | api key of the Face recognition service, created by the user           |
| embeddings   | body        | array  | required | an input embeddings. The length depends on the model (e.g. 512 or 128) |
| image_id     | variable    | UUID   | required | an id of the source embedding within the Face Collection               |

Response body on success:
```json
{
  "result": [
    {
      "embedding": [0.0627421774604647, "...", -0.0236684433507126],
      "similarity": 0.55988
    },
    "..."
  ]
}
```

| Element     | Type   | Description                                                                  |
|-------------|--------|------------------------------------------------------------------------------|
| result      | array  | an array that contains all the results                                       |
| embedding   | array  | a source embedding which we are comparing to embedding from Face Collection  |
| similarity  | float  | a similarity between the source embedding and embedding from Face Collection |

### Face Verification Service, Embedding
The service is used to determine similarities between an input source embedding and input target embeddings. An example:

```shell
curl -X POST "http://localhost:8000/api/v1/verification/embeddings/verify" \
-H "Content-Type: application/json" \
-H "x-api-key: <service_api_key>" \
-d {"source": [<source_embedding>], "targets": [[<target_embedding>], ...]}
```

| Element          | Description | Type    | Required | Notes                                                                                |
|------------------|-------------|---------|----------|--------------------------------------------------------------------------------------|
| Content-Type     | header      | string  | required | application/json                                                                     |
| x-api-key        | header      | string  | required | api key of the Face verification service, created by the user                        |
| source           | body        | array   | required | an input embeddings. The length depends on the model (e.g. 512 or 128)               |
| targets          | body        | array   | required | an array of the target embeddings. The length depends on the model (e.g. 512 or 128) |

Response body on success:
```json
{
  "result": [
    {
      "embedding": [0.0627421774604647, "...", -0.0236684433507126],
      "similarity": 0.55988
    },
    "..."
  ]
}
```

| Element     | Type   | Description                                                        |
|-------------|--------|--------------------------------------------------------------------|
| result      | array  | an array that contains all the results                             |
| embedding   | array  | a target embedding which we are comparing to source embedding      |
| similarity  | float  | a similarity between the source embedding and the target embedding |
