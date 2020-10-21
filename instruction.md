## Project setup and usage

To start working with Face Recognition System, perform the following sequence of steps:
[Read more](./README.md)

## Overview

Face Recognition system is a way of recognizing a human face through technology. A facial recognition system uses facial features from a photograph and compares the information with a database of known faces to find a match.

So how does facial recognition work?

Step 1. You need to sign up to the system (First user in CompareFace admin has Owner role, but it is possible to change the role) and then LogIn with created account or just use the existing one. After that system redirects you to the main page.

Step 2. Create an application (left section) with "Create" link at the bottom of the page. An application is where you can create and manage your face collections.

Step 3. Enter you application with double click on the name of the application. Here you will have two possibilities. The first one is to add new users to your application and manage permissions (Owner and Administrator roles already have access to any application without invite, user role doesn't.) The second one is to create face collections.

Step 4. After creating new collection, it appears at the Face Collections List created within the application with an appropriate name and [API key](#api-key). The user has the possibility to add new Face or to test the existing one (three dots on right side and click "test" link). This option will lead the user to Test Face Collection page, where is the drag&drop to upload image with face to recognize. We recommend an image size no higher than 5MB, as it could slow down the request process. Supported image formats are JPEG/PNG/JPG/ICO/BMP/GIF/TIF/TIFF format.


Step 5. Upload your photo and let Face Recognition system compare faces. When you have face contour detection enabled (green borders around the face). These points represent the shape of the feature. API requests within the solution use RESTful API, and backend data collection. [Read more about API](./README.md) With Face Recognition system APIs you can add Face Recognition capabilities using simple API Calls.

The following result Json illustrates how these points map to a face, where 
1. face_name - name of the trained model
2. similarity (probability) - set the percent match of a detected face with a reference photo  
3. x,y - coordinates of the frame containing the face  
4. Box coordinates are available set values for key in format x_min,x_max,y_min,y_max and the API will provided analysis for just one face

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

# Api Key

By using the created API key, the user can add an image as an example of the face, retrieve a list of saved images, recognize a face from the uploaded image the Face Collection, and delete all examples of the face by the name.

The following JSON illustrates example of Request, where x-api-key - model API key:

                    {
  "Headers": {
    "Content-Type": "multipart/form-data",
    "Origin": "http://exadel.ua:8000",
    "Referer": "http://exadel.ua:8000",
    "host": "exadel.ua:8000",
    "x-api-key": "875d1b68-24b1-4427-9db1-e53cbb71f4f3"
  }
}

Model API  key allows you to add an Image file directly into Image Database through a Post Request:

 -X POST "http://localhost:8000/api/v1/faces/?subject=<face_name>"

Also you can use this Api Key for face recognition using the next Post Request:

  -X POST "http://localhost:8000/api/v1/recognize"




# Project structure and architecture

[Read more](./ui/README.md)
