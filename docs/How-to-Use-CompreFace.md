# How to Use CompreFace


**Step 1.** You need to sign up for the system and log in into the account you’ve just created or use the one you already have. After that, the system redirects you to the main page.

**Step 2.** Create an application (left section) using the "Create" link at the bottom of the page. An application is where you can create and manage your Face Collections.

**Step 3.** Enter your application by clicking on its name. Here you will have two options: you can either add new users and manage 
their access roles or create new [Face Services](Face-services-and-plugins.md).

**Step 4.** To recognize subjects among the known subjects, you need to create Face Recognition Service. After creating a new Face 
Service, you will see it in the Services List with an appropriate name and API key.

**Step 5.** To add known subjects to your Face Collection of Face Recognition Service, you can use REST API. 
Once you’ve uploaded all known faces, you can test the collection using REST API or the TEST page. 
We recommend that you use an image size no higher than 5MB, as it could slow down the request process. The supported image formats include JPEG/PNG/JPG/ICO/BMP/GIF/TIF/TIFF.

**Step 6.** Upload your photo and let our open-source face recognition system match the image against the Face Collection. If you use a 
UI for face recognition, you will see the original picture with marks near every face. If you use REST API, you will receive a response in JSON format.

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
          "subject": "lisan"
        }
      ]
    },
    {
      
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
        let r = await fetch('http://localhost:8000/api/v1/recognition/faces/?subject=`${name}`', {method: "POST", body: formData});
    } catch (e) {
        console.log('Houston, we have a problem...:', e);
    }
}
```

This function sends the image to our server and shows results in a text area:

```js
function recognizeFace(input) {

    async function getData() {
        let response = await fetch('http://localhost:8000/api/v1/recognition/recognize')
        let data = await response.json()
        return data
    }

    let result = Promise.resolve(response)
    result.then(data => {
        document.getElementById("result-textarea-request").innerHTML = JSON.stringify(data);
    });
}
```
