

<h3 align="center">Face Recognition & Access Control System</h3>


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


<hr>

# Table Of Contents

  * [Overview](#overview)
  * [Screenshots](#screenshots)
  * [Video tutorials](#videos)
  * [News and updates](#news-and-updates)
  * [Features](#features)
  * [Functionalities](#functionalities)
  * [Getting Started with CompreFace](#getting-started-with-compreface)
  * [CompreFace SDKs](#compreface-sdks)
  * [Documentation](/docs)
    * [How to Use CompreFace](/docs/How-to-Use-CompreFace.md)
    * [Face Services and Plugins](/docs/Face-services-and-plugins.md)
    * [Rest API Description](/docs/Rest-API-description.md)
    * [Postman documentation and collection](https://documenter.getpostman.com/view/17578263/UUxzAnde)
    * [Face Recognition Similarity Threshold](/docs/Face-Recognition-Similarity-Threshold.md)
    * [Configuration](/docs/Configuration.md)
    * [Architecture and Scalability](/docs/Architecture-and-scalability.md)
    * [Custom Builds](/docs/Custom-builds.md)
    * [Face data migration](/docs/Face-data-migration.md)
    * [User Roles System](/docs/User-Roles-System.md)
    * [Face Mask Detection Plugin](/docs/Mask-detection-plugin.md)
    * [Kubernetes configuration](https://github.com/exadel-inc/compreface-kubernetes)
    * [Gathering Anonymous Statistics](/docs/Gathering-anonymous-statistics.md)
    * [Installation Options](/docs/Installation-options.md)
  * [Contributing](#contributing)
  * [License info](#license-info)

# Overview


** Applications:**
- Base access control and perimeter security
- Personnel attendance and duty tracking
- Secure facility access management
- Operational deployment personnel tracking
- Unauthorized intruder detection and alerts
- Integration with existing CCTV infrastructure



Optimisé pour Apple Silicon (M3 Max avec accélération GPU MPS) et déployé en tant que services conteneurisés avec support pour le traitement CPU et GPU.
Construit sur un chiffrement de niveau militaire et des modèles d'IA de pointe (FaceNet, InsightFace) avec support d'intégration pour les caméras de surveillance Hikvision 8MP.

# Screenshots

<p align="center">
<img src="https://github.com/exadel-inc/CompreFace/assets/3736126/7b86a96f-844b-4e4b-9456-c53f6e45f305" 
alt="compreface-recognition-page" width=390px style="padding: 0px 10px 0px 0px;">
<img src="https://github.com/exadel-inc/CompreFace/assets/3736126/51efb9d0-70cc-4902-bc3f-fd85de004b67" 
alt="compreface-dashboard-page" width="390px" style="padding: 0px 0px 0px 10px;">
</p>

<details>
  <summary> <b>More Screenshots</b> </summary>
  <!-- have to be followed by an empty line! -->

<p align="center">
<img src="https://github.com/exadel-inc/CompreFace/assets/3736126/3ae0ce68-588b-4370-8eaf-32668c96fa63"
alt="compreface-verification-page" width=390px style="padding: 0px 10px 0px 0px;">
<img src="https://github.com/exadel-inc/CompreFace/assets/3736126/9246702d-1c9b-4435-8098-89e0fb616b0d"
alt="compreface-detection-page" width="390px" style="padding: 0px 0px 0px 10px;">
</p>
<p align="center">
<img src="https://github.com/exadel-inc/CompreFace/assets/3736126/3a5787e6-9a85-4852-92dc-a82fe7ef8f7c" 
alt="compreface-services-page" width=390px style="padding: 0px 10px 0px 0px;">
<img src="https://github.com/exadel-inc/CompreFace/assets/3736126/e7fd0258-2643-4cec-809d-988502eb857f" 
alt="compreface-wizzard-page" width="390px" style="padding: 0px 0px 0px 10px;">
</p>

</details>

# Videos

<p align="center">
<a target="_blank" href="https://www.youtube.com/watch?v=LS4sVTnI-gI">
     <img src="https://user-images.githubusercontent.com/3736126/241272669-8609463b-8b22-4ae7-bf21-36761f00734b.jpg" 
        alt="CompreFace Face Detection Demo" width=390px style="padding: 0px 10px 0px 0px;">
</a>
<a target="_blank" href="https://www.youtube.com/watch?v=jkiA3S-LYSk">
     <img src="https://user-images.githubusercontent.com/3736126/242002411-3c06d3f7-c0ac-49f8-ac79-42bd8c431570.png" 
        alt="CompreFace Appery.io Demo" width=390px style="padding: 0px 10px 0px 0px;">
</a>
</p>

<details>
  <summary> <b>More Videos</b> </summary>
  <!-- have to be followed by an empty line! -->

<p align="center">
<a target="_blank" href="https://www.youtube.com/watch?v=cF3P7bTJXY0">
     <img src="https://user-images.githubusercontent.com/3736126/241274256-0dc6d8a0-91d5-42c4-b029-200b72bb169b.jpg" 
        alt="CompreFace .NET SDK Demo" width=390px style="padding: 0px 10px 0px 0px;">
</a>
<a target="_blank" href="https://www.youtube.com/watch?v=9mQULPrTVP4">
     <img src="https://user-images.githubusercontent.com/3736126/241274522-a152221f-e382-416c-9a71-f7694e73cf3e.jpg" 
        alt="CompreFace JavaScript SDK Demo" width=390px style="padding: 0px 10px 0px 0px;">
</a>
</p>

</details>

# News and updates

[Subscribe](https://info.exadel.com/en/compreface-news-and-updates) to CompreFace News and Updates to never miss new features and product improvements.

# Features
The Face Recognition System provides biometric identification capabilities. System can accurately identify personnel even from a single enrollment photo.



# Functionalities

- Supports many face recognition services:
  - [face detection](/docs/Face-services-and-plugins.md#face-detection)
  - [face recognition](/docs/Face-services-and-plugins.md#face-recognition)
  - [face verification](/docs/Face-services-and-plugins.md#face-verification)
  - [landmark detection plugin](/docs/Face-services-and-plugins.md#face-plugins)
  - [age recognition plugin](/docs/Face-services-and-plugins.md#face-plugins)
  - [gender recognition plugin](/docs/Face-services-and-plugins.md#face-plugins)
  - [face mask detection plugin](/docs/Face-services-and-plugins.md#face-plugins)
  - [head pose plugin](/docs/Face-services-and-plugins.md#face-plugins)
- Use the CompreFace UI panel for convenient user roles and access management

# Getting Started with CompreFace

### Requirements

1. Docker and Docker compose (or Docker Desktop)
2. CompreFace could be run on most modern computers with [x86 processor](https://en.wikipedia.org/wiki/X86) and [AVX support](https://en.wikipedia.org/wiki/Advanced_Vector_Extensions).
   To check AVX support on Linux run `lscpu | grep avx` command

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

### Getting started for contributors

Follow this [link](/dev)

# CompreFace SDKs

| SDK        | Repository                                              |
|------------|---------------------------------------------------------|
| JavaScript | https://github.com/exadel-inc/compreface-javascript-sdk |
| Python     | https://github.com/exadel-inc/compreface-python-sdk     |
| .NET       | https://github.com/exadel-inc/compreface-net-sdk        |



# License info 

CompreFace is open-source real-time facial recognition software released under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.html).
