##
# **Overview**

CompreFace is an application for facial recognition that can be integrated as a standalone server or deployed on the cloud and can be set up and used without machine learning expertise.

Our method is based on deep neural networks, which is one of the most popular facial recognition methods and provides a convenient API for model training and face recognition. We also provide an easy-to-understand roles system with which you can easily control who has access to the model.

Every user can create their own models and train them on different subsets of input data.

##
# **Features**

The system can accurately identify people even when it is only given one example of their face.

CompreFace:

- Uses open-source code and operates fully on-premises for data security
- Can be set up and used without machine learning expertise
- Uses one of the most popular face recognition methods for highest accuracy
- Includes a UI panel with roles for access control
- Starts quickly with one docker command

##
# **Getting Started**

To get started:

1. Install Docker
2. Download the archive from our latest release: [https://github.com/exadel-inc/CompreFace/releases](https://github.com/exadel-inc/CompreFace/releases)
3. Unzip the archive
4. Run Docker
5. Windows search bar-\&gt; cmd-\&gt;in the Command prompt-\&gt; cd -\&gt;paste the path to the extracted zip folder
6. Run command: docker-compose up
7. Open [http://localhost:8000/](http://localhost:8000/)

Getting started for Contributors:

1. Install Docker
2. Clone repository
3. Open dev folder
4. Run command: docker-compose up --build
5. Open [http://localhost:8000/](http://localhost:8000/)

\*\* Tips for Windows\*\* (use Git Bash terminal)

1. Turn of the git autocrlf with command: git config --global core.autocrlf false
2. Make sure all your containers are down: $ docker ps
3. In case some containers are working, they should be stopped: $ docker-compose down
4. Clean all local datebases and images: docker system prune --volumes
5. Last line in /dev/start.sh file change to docker-compose -f docker-compose.yml up --remove-orphans --build
6. Go to Dev folder cd dev
7. Run sh start.sh and make sure [http://localhost:8000/](http://localhost:8000/) starts
8. Stopped all containers: $ docker-compose down
9. Run sh start--dev.sh and make sure [http://localhost:4200/](http://localhost:4200/) starts

##
# **Set Up Overview**

1. Register users in the app
2. Create applications and models; invite users
3. Integrate your app via API if needed
4. Upload images
5. Train a model with your own images by using the API key
6. Send a new image for facial recognition.

![](RackMultipart20201009-4-ogl70f_html_996e30a11d48d75c.png)

##
# **How it Works**

**Find a face**

CompreFace can detect one or more faces in an image. The basis for our program is Multi-Task Cascaded Convolutional Neural Networks (MTCNN).

**Pose and project faces**

CompreFace handles the normalization of all found faces with rotate, scale and shear functions.

**Calculate embedding from faces**

CompreFace will calculate embedding and classify the face based on extracted features. We took Convolutional Neural Networks (CNN) for face recognition and removed the last three fully connected layers. As a result, Neural Networks calculates embedding.

**Use embedding for training the model and recognizing faces**

Haifengl/smile [LogisticRegression](http://haifengl.github.io/api/java/smile/classification/LogisticRegression.html) was used as a classifier to recognize people in photos. Recognizing the person in the photo.

Machine Learning Technologies and Methods Incorporated into CompreFace

- [MTCNN (Multi-Task Cascaded Convolutional Networks)](https://arxiv.org/pdf/1604.02878.pdf)
- [FaceNet](https://github.com/davidsandberg/facenet)
- Logistic Regression

### Referenced Machine Learning Papers and Algorithms

- **FaceNet: A Unified Embedding for Face Recognition and Clustering** Florian Schroff, Dmitry Kalenichenko, James Philbin (Submitted on 17 Jun 2015)
- **Joint Face Detection and Alignment using Multi-task Cascaded Convolutional Neural Networks** Kaipeng Zhang, Zhanpeng Zhang, Zhifeng Li, Yu Qiao (Submitted on 11 Apr 2016)
- **Inception-v4, Inception-ResNet and the Impact of Residual Connections on Learning** Christian Szegedy, Sergey Ioffe, Vincent Vanhoucke, Alex Alemi (Submitted on 23 Aug 2016)

##
# **Technologies**

### Architecture diagram

![](RackMultipart20201009-4-ogl70f_html_e9d00dd585deb7e1.png)

### Database

- PostgreSQL

### Platform server

- Java 11
- Spring Boot

### API server

- Java 11
- Spring Boot
- Haifengl/Smile

### Embedding server

- Python
- [FaceNet](https://github.com/davidsandberg/facenet)
- [InsightFace](https://github.com/deepinsight/insightface)
- TensorFlow
- SciPy
- NumPy
- OpenCV (for images resizing)

##
# **Rest API Description**

By using the created API key, the user can add an image as an example of the face, retrieve a list of saved images, recognize a face from the uploaded image, retrain the model, and delete all examples of the face by the name.

### **Add an Example of a Particular Face**

Provide the model with an example of a person&#39;s face from saved images. You can add as many images as you want to train the system.

curl -X POST &quot;http://localhost:8000/api/v1/faces/?subject=\&lt;face\_name\&gt;&amp;retrain=\&lt;retrain\_option\&gt;&quot; \

-H &quot;Content-Type: multipart/form-data&quot; \

-H &quot;x-api-key: \&lt;model\_api\_key\&gt;&quot; \

-F file=@\&lt;local\_file\&gt; \

-F det\_prob\_threshold=@\&lt;det\_prob\_threshold\&gt; \

-F retrain=@\&lt;retrain\_option\&gt; \

| **Element** | **Description** | **Type** | **Required** | **Notes** |
| --- | --- | --- | --- | --- |
| Content-Type | header | string | required | multipart/form-data |
| x-api-key | header | string | required | api key of the model, created by the user |
| face\_name | param | string | required | is the name you assign to the image you save |
| file | body | image | required | allowed image formats: jpeg, jpg, ico, png, bmp, gif, tif, tiff, webp. Max size is 5Mb |
| det\_prob\_ threshold | body | string | optional | minimum required confidence that a recognized face is actually a face. Value is between 0.0 and 1.0 |
| retrain\_option | body | string | optional | retrains the model for the specified API key. Can be set to &quot;yes&quot;, &quot;no&quot;, &quot;force&quot;. Default value: &quot;force&quot; |

Response body on success:

{

&quot;image\_id&quot;: &quot;\&lt;UUID\&gt;&quot;,

&quot;subject&quot;: &quot;\&lt;face\_name\&gt;&quot;

}

| **Element** | **Type** | **Description** |
| --- | --- | --- |
| image\_id | UUID | UUID of uploaded image |
| subject | string | \&lt;face\_name\&gt; of saved image |

### **Recognize a Face**

To enable facial recognition from uploaded images, input: curl -X POST &quot;http://localhost:8000/api/v1/recognize&quot; \

-H &quot;Content-Type: multipart/form-data&quot; \

-H &quot;x-api-key: \&lt;model\_api\_key\&gt;&quot; \

-F file=\&lt;local\_file\&gt;

-F limit=\&lt;limit\&gt;

-F prediction\_count=\&lt;prediction\_count\&gt;

| **Element** | **Description** | **Type** | **Required** | **Notes** |
| --- | --- | --- | --- | --- |
| Content-Type | header | string | required | multipart/form-data |
| x-api-key | header | string | required | api key of the model, created by the user |
| file | body | image | required | allowed image formats: jpeg, jpg, ico, png, bmp, gif, tif, tiff, webp. Max size is 5Mb |
| limit | body | integer | optional | maximum number of faces to be recognized. Value of 0 represents no limit. Default value: 0 |
| prediction\_count | body | integer | optional | maximum number of predictions per faces. Default value: 1 |

Response body on success:

{

&quot;result&quot;: [

{

&quot;box&quot;: {

&quot;probability&quot;: \&lt;probability\&gt;,

&quot;x\_max&quot;: \&lt;integer\&gt;,

&quot;y\_max&quot;: \&lt;integer\&gt;,

&quot;x\_min&quot;: \&lt;integer\&gt;,

&quot;y\_min&quot;: \&lt;integer\&gt;

},

&quot;faces&quot;: [

{

&quot;similarity&quot;: \&lt;similarity1\&gt;,

&quot;subject&quot;: \&lt;face\_name1\&gt;

},

...

]

}

]

}

| **Element** | **Type** | **Description** |
| --- | --- | --- |
| box | object | list of parameters of the bounding box for this face |
| probability | float | probability that a found face is actually a face |
| x\_max, y\_max, x\_min, y\_min | integer | coordinates of the frame containing the face |
| faces | list | list of similar faces with size of \&lt;prediction\_count\&gt; order by similarity |
| similarity | float | similarity that on that image predicted person |
| subject | string | name of the subject in model |

### **Get a List of Saved Images**

To retrieve a list of images saved in a model:

curl -X GET &quot;http://localhost:8000/api/v1/faces&quot; \

-H &quot;x-api-key: \&lt;model\_api\_key\&gt;&quot; \

| **Element** | **Description** | **Type** | **Required** | **Notes** |
| --- | --- | --- | --- | --- |
| x-api-key | header | string | required | api key of the model, created by the user |

Response body on success:

{

&quot;faces&quot;: [

{

&quot;image\_id&quot;: \&lt;face\_id\&gt;,

&quot;subject&quot;: \&lt;face\_name\&gt;

},

...

]

}

| **Element** | **Type** | **Description** |
| --- | --- | --- |
| image\_id | UUID | UUID of the face |
| subject | string | \&lt;face\_name\&gt; of the person, whose picture was saved for this api key |

### **Delete Examples of a Face**

The following deletes all image examples of \&lt;face\_name\&gt;:

curl -X DELETE &quot;http://localhost:8000/api/v1/faces/?subject=\&lt;face\_name\&gt;&amp;retrain=\&lt;retrain\_option\&gt;&quot; \

-H &quot;x-api-key: \&lt;model\_api\_key\&gt;&quot;

| **Element** | **Description** | **Type** | **Required** | **Notes** |
| --- | --- | --- | --- | --- |
| x-api-key | header | string | required | api key of the model, created by the user |
| face\_name | param | string | optional | is the name you assign to the image you save. Caution! If this parameter is absent, all faces in model will be removed |
| retrain\_option | param | string | optional | retrains the model after deleting. Can be set to &quot;yes&quot;, &quot;no&quot;, &quot;force&quot;. Default value: &quot;force&quot; |
| Response body on success: |
 |
 |
 |
 |

[

{

&quot;image\_id&quot;: \&lt;face\_id\&gt;,

&quot;subject&quot;: \&lt;face\_name\&gt;

},

...

]

| **Element** | **Type** | **Description** |
| --- | --- | --- |
| image\_id | UUID | UUID of the removed face |
| subject | string | \&lt;face\_name\&gt; of the person, whose picture was saved for this api key |

### **Delete Examples of a Face by ID**

To delete images by ID:

curl -X DELETE &quot;http://localhost:8000/api/v1/faces/\&lt;image\_id\&gt;retrain=\&lt;retrain\_option\&gt;&quot; \

-H &quot;x-api-key: \&lt;model\_api\_key\&gt;&quot;

| **Element** | **Description** | **Type** | **Required** | **Notes** |
| --- | --- | --- | --- | --- |
| x-api-key | header | string | required | api key of the model, created by the user |
| image\_id | variable | UUID | required | UUID of the removing face |
| retrain\_option | param | string | optional | retrains the model after deleting. Can be set to &quot;yes&quot;, &quot;no&quot;, &quot;force&quot;. Default value: &quot;force&quot; |
| Response body on success: |
 |
 |
 |
 |

{

&quot;image\_id&quot;: \&lt;face\_id\&gt;,

&quot;subject&quot;: \&lt;face\_name\&gt;

}

| **Element** | **Type** | **Description** |
| --- | --- | --- |
| image\_id | UUID | UUID of the removed face |
| subject | string | \&lt;face\_name\&gt; of the person, whose picture was saved for this api key |

##
# **Contribute to CompreFace&#39;s Open-Source Code**

Contributions are welcome and greatly appreciated.

After creating your first contributing Pull Request you will receive a request to sign our Contributor License Agreement by commenting your PR with a special message.

### **Formatting Standards**

For Java just import dev/team\_codestyle.xml file in your IntelliJ IDEA.

### **Report Bugs**

Report bugs at [https://github.com/exadel-inc/CompreFace/issues](https://github.com/exadel-inc/CompreFace/issues).

Please include:

- Your operating system name and version
- Any details about your local setup that might be helpful in troubleshooting
- Detailed steps to reproduce the bug

### **Submit Feedback**

The best way to send feedback is to file an issue at [https://github.com/exadel-inc/CompreFace/issues](https://github.com/exadel-inc/CompreFace/issues).

If you are proposing a feature, please:

- Explain in detail how it should work.
- Keep the scope as narrow as possible to make it easier to implement.

## **License info**

CompreFace is Open Source software released under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.html).

**New Short Description:**

Open-source facial recognition system from Exadel

CompreFace is an open-source facial recognition program. It relies on MTCNN for quick and accurate recognition. Our easy-to-use program does not require an in-depth knowledge of machine learning. Simply input an example of a face, upload images, and allow CompreFace to find and (if necessary) delete all images of that face from your uploaded images.
