
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
  * [CompreFace SDKs](#compreFace-SDKs)
  * [Documentation](/docs)
    * [How to Use CompreFace](/docs/How-to-Use-CompreFace.md)
    * [Face Services and Plugins](/docs/Face-services-and-plugins.md)
    * [Rest API Description](/docs/Rest-API-description.md)
    * [Face Recognition Similarity Threshold](/docs/Face-Recognition-Similarity-Threshold.md)
    * [Configuration](/docs/Configuration.md)
    * [Architecture and Scalability](/docs/Architecture-and-scalability.md)
    * [Custom Builds](/docs/Custom-builds.md)
    * [Face data migration](/docs/Face-data-migration.md)
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

### Getting started for devs

Follow this [link](/dev)

# CompreFace SDKs

| SDK        | Repository   | 
| ---------- | ------ | 
| JavaScript | https://github.com/exadel-inc/compreface-javascript-sdk   |

# Documentation

More documentation is available [here](/docs)

# Contributing

We want to improve our open-source face recognition solution, so your contributions are welcome and greatly appreciated. 
After creating your first contributing pull request, you will receive a request to sign our Contributor License Agreement by commenting your pull request with a special message.

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
