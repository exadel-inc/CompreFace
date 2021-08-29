# Face Services and Plugins

CompreFace supports these face services and plugins:
* Face recognition service (Face identification)
* Face detection service
* Face verification service
* Age detection plugin
* Gender detection plugin
* Landmarks detection plugin
* Calculator plugin

## Services

To use face service you need to create it in an application on UI. The type of service depends on your application needs. Each service has its own REST API context and there is no possibility to change the service type after creation. Here is a short description of each of them:
* Face recognition service is used for face identification. This means that you first need to upload known faces to faces collection and then recognize unknown faces among them. When you upload an unknown face, the service returns the most similar faces to it. Also, face recognition service supports verify endpoint to check if this person from face collection is the correct one. The possible cases include:
  * when you have photos of employees and want to recognize strangers in the office
  * when you have photos of conference attendees and want to track who was interested in which topics.
  * when you have photos of VIP guests and you want to find them among the crowd very quickly.
* Face verification service is used to check if this person is the correct one. The service compares two faces you send to the rest endpoint and returns their similarity. The possible cases include:
  * when a customer provides you an ID or driving license and you need to verify if this is him
  * when a user connects his social network account to your application and you want to verify if this is him
* Face detection service is used to detect all faces in the image. It doesn’t recognize faces, just find them on the image. The most useful cases include face plugins for face analysis:
  * gather statistics on how your store popular among different genders
  * gather statistics on among what ages your event is popular
  * get landmark information to know where customers look at
  * gather statistics on how many customers in the store

##  Face plugins

Face plugins could be used with any of the face services. By default, face services return only bounding boxes and similarity if 
applicable. To add more information in response you can add face plugins in your request. To add a plugin you need to list 
comma-separated needed plugins in the query `face_plugins` parameter. This parameter is supported by all face recognition services.
Example:
```shell
curl  -X POST "http://localhost:8000/api/v1/recognition/recognize?face_plugins=age,gender,landmarks,mask" \
-H "Content-Type: multipart/form-data" \
-H "x-api-key: <faces_recognition_api_key>" \
-F file=<local_file>
```
This request will recognize faces on the image and return additional information about age, gender, and landmarks.

The list of possible plugins:
* age - returns the supposed range of a person’s age in format [min, max]
* gender - returns the supposed person’s gender
* landmarks - returns face landmarks. This plugin is supported by all configurations and returns 5 points of eyes, nose, and mouth
* calculator - returns face embeddings.  
* mask - returns if the person wears a mask. Possible results: `without_mask`, `mask_worn_incorrectly`, `mask_worn_correctly`. Learn more about [mask plugin](Mask-detection-plugin.md)
* landmarks2d106 - returns face landmarks. This plugin is supported only by the configuration that uses insightface library. It’s not 
  available by default. More information about landmarks [here](https://github.com/deepinsight/insightface/tree/master/alignment/coordinateReg#visualization).
