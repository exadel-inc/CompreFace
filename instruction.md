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

The following JavaScript code example allows a user to choose an image and send it to the server. The server side script will recieve the file, and then unencoded it using CompreFace API.
To run the JavaScript example just load the source code into an browser console.

async function UploadPhoto()
{
let user = { face_name:'john'};
let formData = new FormData();
let photo = document.getElementById("fileDropRef").files[0];

    formData.append("photo", photo);
    formData.append("user", JSON.stringify(user));

    try {
       let r = await fetch('http://exadel.ua:8000/api/v1/faces/?subject=<user.face_name>', {method: "POST", body: formData});
     } catch(e) {
       console.log('Huston we have the problem...:', e);
    }

}

HTML:

<div class="dropzone">
    <div class="dropzone-icon">
        <img src="../../../assets/some_image" alt="image">
    </div>
    <div class="dropzone-content">
        <h4>title</h4>
        <label>label</label>
    </div>

    <input type="file" #fileDropRef id="fileDropRef" />

</div>

After requesting JSON data from a server (box Face coordinates) and determining the image size with recalculating according to canvas size, UI will draw the canvas using HTMLCanvasElement.getContext() method. Next JS code example makes canvas and draw face and info on image.

drawCanvas(box, face) {
    const img = new Image();
    const resultFace = face.length > 0 ? face[0] : { face_name: undefined, similarity: 0 };
    const ctx: CanvasRenderingContext2D = this.HTMLCanvasElement.getContext('2d');

    img.onload = () => {
      ctx.drawImage(img, 0, 0, this.canvasSize.width, this.canvasSize.height);
      ctx.beginPath();
      ctx.strokeStyle = 'green';
      ctx.moveTo(box.x_min, box.y_min);
      ctx.lineTo(box.x_max, box.y_min);
      ctx.lineTo(box.x_max, box.y_max);
      ctx.lineTo(box.x_min, box.y_max);
      ctx.lineTo(box.x_min, box.y_min);
      ctx.stroke();
      ctx.fillStyle = 'green';
      ctx.fillRect(box.x_min, box.y_min - 25, box.x_max - box.x_min, 25);
      ctx.fillRect(box.x_min, box.y_max, box.x_max - box.x_min, 25);
      ctx.fillStyle = 'white';
      ctx.font = '12pt Roboto Regular Helvetica Neue sans-serif';
      ctx.fillText(resultFace.similarity, box.x_min + 10, box.y_max + 20);
      ctx.fillText(resultFace.face_name, box.x_min + 10, box.y_min - 5);
    };
    img.src = URL.createObjectURL(this.file);
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

Model API key allows you to add an Image file directly into Image Database through a Post Request:

-X POST "http://localhost:8000/api/v1/faces/?subject=<face_name>"

Also you can use this Api Key for face recognition using the next Post Request:

-X POST "http://localhost:8000/api/v1/recognize"

# Project structure and architecture

[Read more](./ui/README.md)
