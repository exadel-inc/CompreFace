# How to Use CompreFace

**Step 1.** Install and run CompreFace using our [Getting Started guide](../README.md#getting-started-with-compreface)

**Step 2.** You need to sign up for the system and log in into the account you’ve just created or use the one you already have. After that, the system redirects you to the main page.

**Step 3.** Create an application (left section) using the "Create" link at the bottom of the page. An application is where you can create and manage your Face Collections.

**Step 4.** Enter your application by clicking on its name. Here you will have two options: you can either add new users and manage 
their access roles or create new [Face Services](Face-services-and-plugins.md).

**Step 5.** To recognize subjects among the known subjects, you need to create Face Recognition Service. After creating a new Face 
Service, you will see it in the Services List with an appropriate name and API key. After this step, you can look at our [demos](#demos).

**Step 6.** To add known subjects to your Face Collection of Face Recognition Service, you can use REST API. 
Once you’ve [uploaded all known faces](Rest-API-description.md#add-an-example-of-a-subject),
you can test the collection using [REST API](Rest-API-description.md#recognize-faces-from-a-given-image) or the TEST page. 
We recommend that you use an image size no higher than 5MB, as it could slow down the request process. The supported image formats include JPEG/PNG/JPG/ICO/BMP/GIF/TIF/TIFF.

**Step 7.** Upload your photo and let our open-source face recognition system match the image against the Face Collection. If you use a 
UI for face recognition, you will see the original picture with marks near every face. If you use [REST API](Rest-API-description.md#recognize-faces-from-a-given-image), you will receive a response in JSON format.

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
      "subjects": [
        {
          "similarity": 0.99593,
          "subject": "lisan"
        }
      ]
    },
    {
      
    }
  ]
}
```

## Demos

1. [tutorial_demo.html](./demos/tutorial_demo.html)

This demo shows the most simple example of Face recognition service usage. 
To run demo, just open html file in browser. 
API key for this demo was created on **step 5** of [How to Use CompreFace](#how-to-use-compreface)

2. [webcam_demo.html](./demos/webcam_demo.html)

This demo shows the most simple webcam demo for Face recognition service.
To run demo, just open html file in browser.
API key for this demo was created on **step 5** of [How to Use CompreFace](#how-to-use-compreface)

## Code Snippets

Here is a JavaScript code snippet that loads a new image to your Face Collection:

```js
function saveNewImageToFaceCollection(elem) {
    let subject = encodeURIComponent(document.getElementById("subject").value);
    let apiKey = document.getElementById("apiKey").value;
    let formData = new FormData();
    let photo = elem.files[0];

    formData.append("file", photo);

    fetch('http://localhost:8000/api/v1/recognition/faces/?subject=' + subject,
        {
            method: "POST",
            headers: {
                "x-api-key": apiKey
            },
            body: formData
        }
    ).then(r => r.json()).then(
        function (data) {
            console.log('New example was saved', data);
        })
        .catch(function (error) {
            alert('Request failed: ' + JSON.stringify(error));
        });
}
```

This function sends the image to our server and shows results in a text area:

```js
function recognizeFace(elem) {
    let apiKey = document.getElementById("apiKey").value;
    let formData = new FormData();
    let photo = elem.files[0];

    formData.append("file", photo);

    fetch('http://localhost:8000/api/v1/recognition/recognize',
        {
            method: "POST",
            headers: {
                "x-api-key": apiKey
            },
            body: formData
        }
    ).then(r => r.json()).then(
        function (data) {
            document.getElementById("result").innerHTML = JSON.stringify(data);
        })
        .catch(function (error) {
            alert('Request failed: ' + JSON.stringify(error));
        });
}
```
