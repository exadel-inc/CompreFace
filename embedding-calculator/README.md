![Example output image](./sample_images/readme_example.png)

# embedding-calculator
This is a component of CompreFace. CompreFace is a service for face recognition: upload images with faces of known people, then upload a new image, and the service will recognize faces in it.

# Setup environment
Not needed if only running containers:
```
$ python -m pip install -r requirements.txt -e srcext/insightface/python-package
$ imageio_download_bin freeimage
```
Only needed if using tools:
```
$ make tools/tmp
$ chmod +x tools/test_memory_constraints.sh
```

# Run service
### Locally
```
$ export FLASK_ENV=development
$ python -m src.app
```

### Docker

##### Images on DockerHub 

There are some pre-build images on https://hub.docker.com/r/exadel/compreface-core. To use it run:
```
$ docker run -p 3000:3000 exadel/compreface-core:latest
```

###### DockerHub tags

| Tag                    | Scanner     | Build arguments                                                                                          | Comment                            |
|------------------------|-------------|----------------------------------------------------------------------------------------------------------|------------------------------------|
| :0.5.1 :latest         | Facenet2018 |                                                                                                          |                                    |
| :0.5.1-insightface     | InsightFace | FACE_DETECTION_PLUGIN=insightface.FaceDetector<br>CALCULATION_PLUGIN=insightface.Calculator              |                                    |
| :0.5.1-insightface-gpu | InsightFace | FACE_DETECTION_PLUGIN=insightface.FaceDetector<br>CALCULATION_PLUGIN=insightface.Calculator<br>GPU_IDX=0 | CORE_GPU_IDX - index of GPU-device |


##### Build
Builds container (also runs main tests during the build):
```
$ docker build -t embedding-calculator .
```
To skip tests during build, use:
```
$ docker build -t embedding-calculator --build-arg SKIP_TESTS=true .
```

##### Run
```
$ docker run -p 3000:3000 embedding-calculator
```

### Run tests
Unit tests
```
$ pytest -m "not integration and not performance" src tools
```
Integration tests
```
$ pytest -m integration src tools
```
Performance tests
```
$ pytest -m performance src tools
```
Lint checks
```
$ python -m pylama --options pylama.ini src tools
```

### Plugins

If DockerHub images is not enough, build an image with only the necessary set of plugins.  
For changing default plugins pass needed plugin names in build arguments and build your own image.

##### Face detection and calculation plugins

Set plugins by build arguments `FACE_DETECTION_PLUGIN` and `CALCULATION_PLUGIN`

| Plugin name              | Slug       | Backend     | Framework  | GPU support |
|--------------------------|------------|-------------|------------|-------------|
| facenet.FaceDetector     | detector   | MTCNN       | Tensorflow |             |
| facenet.Calculator       | calculator | Facenet     | Tensorflow |             |
| insightface.FaceDetector | detector   | insightface | MXNet      |      +      |
| insightface.Calculator   | calculator | insightface | MXNet      |      +      |

##### Extra plugins

Pass to `EXTRA_PLUGINS` comma-separated names of plugins. 

| Plugin name                        | Slug           | Backend     | Framework  | GPU support |
|------------------------------------|----------------|-------------|------------|-------------|
| agegender.AgeDetector              | age            | agegender   | Tensorflow |             |
| agegender.GenderDetector           | gender         | agegender   | Tensorflow |             |
| insightface.AgeDetector            | age            | insightface | MXNet      | +           |
| insightface.GenderDetector         | gender         | insightface | MXNet      | +           |
| facenet.LandmarksDetector          | landmarks      | Facenet     | Tensorflow | +           |
| insightface.LandmarksDetector      | landmarks      | insightface | MXNet      | +           |
| insightface.Landmarks2d106Detector | landmarks2d106 | insightface | MXNet      | +           |
| facenet.facemask.MaskDetector      | mask           | facemask    | Tensorflow | +           |
| insightface.facemask.MaskDetector  | mask           | facemask    | MXNet      | +           |
| facenet.PoseEstimator              | pose           | Facenet     | Tensorflow | +           |
| insightface.PoseEstimator          | pose           | insightface | MXNet      | +           |

Notes:    
* `facenet.LandmarksDetector` and `insightface.LandmarksDetector` extract landmarks
  from results of `FaceDetector` plugin without additional processing. Returns 5 points of eyes, nose and mouth.
* `insightface.Landmarks2d106Detector` detects 106 points of facial landmark.
  [Points mark-up](https://github.com/deepinsight/insightface/tree/master/alignment/coordinateReg#visualization) 
      

##### Default build arguments:
```
FACE_DETECTION_PLUGIN=facenet.FaceDetector
CALCULATION_PLUGIN=facenet.Calculator
EXTRA_PLUGINS=facenet.LandmarksDetector,agegender.GenderDetector,agegender.AgeDetector,facenet.facemask.MaskDetector,facenet.PoseEstimator
```

#### Pre-trained models

Some plugins have several pre-trained models.  
To use an additional model pass a name of the model after a plugin name with a separator `@`. For example:
```
FACE_DETECTION_PLUGIN=insightface.FaceDetector@retinaface_mnet025_v1
```

List of pre-trained models:

* facenet.Calculator 
    * 20180402-114759 (default)
    * 20180408-102900

* insightface.FaceDetector 
    * retinaface_r50_v1 (default)
    * retinaface_mnet025_v1
    * retinaface_mnet025_v2
    
* insightface.Calculator 
    * arcface_r100_v1 (default)
    * arcface_resnet34
    * arcface_resnet50
    * arcface_mobilefacenet
    * [arcface-r50-msfdrop75](https://github.com/deepinsight/insightface/tree/master/recognition/SubCenter-ArcFace)
    * [arcface-r100-msfdrop75](https://github.com/deepinsight/insightface/tree/master/recognition/SubCenter-ArcFace)
  
* facenet.facemask.MaskDetector
    * inception_v3_on_mafa_kaggle123 (default)
    * mobilenet_v2_on_mafa_kaggle123
  
* insightface.facemask.MaskDetector
    * mobilenet_v2_on_mafa_kaggle123 (default)
    * resnet18_on_mafa_kaggle123


#### Optimization 

There are two build arguments for optimization:
* `GPU_IDX` - id of NVIDIA GPU device, starts from `0` (empty or `-1` for disable)
* `INTEL_OPTIMIZATION` - enable Intel MKL optimization (true/false)


##### GPU Setup (Windows):
1. Install or update Docker Desktop.
2. Make sure that you have Windows version 21H2 or higher.
3. Update your NVIDIA drivers.
4. Install or update WSL2 Linux kernel.
5. Make sure the WSL2 backend is enabled in Docker Desktop.

##### GPU Setup (Linux): 

Install the nvidia-docker2 package and dependencies on the host machine:
```
sudo apt-get update
sudo apt-get install -y nvidia-docker2
sudo systemctl restart docker
```

##### Build and run with enabled gpu
```
docker build . -t embedding-calculator-cuda -f gpu.Dockerfile
docker build . -t embedding-calculator-gpu --build-arg GPU_IDX=0 --build-arg BASE_IMAGE=embedding-calculator-cuda
docker run -p 3000:3000 --gpus all embedding-calculator-gpu
```

# Tools
Finds faces in a given image, puts bounding boxes and saves the resulting image. 
```
$ export IMG_NAMES=015_6.jpg
$ python -m tools.scan
```

Tests the accuracy of face detection.
```
$ make tools/benchmark_detection/tmp
$ python -m tools.benchmark_detection
```

Tests whether service crashes with various parameters under given RAM constraints.
```
$ docker build -t embedding-calculator .
$ tools/test_memory_constraints.sh $(pwd)/sample_images
```

Optimizes face detection library parameters with a given annotated image dataset.
```
$ mkdir tmp
$ python -m tools.optimize_detection_params
```

# Benchmark

Perform the following steps:
1. [Build and run](#build) `embedding-calculator` with the needed scanner backend and CPU/GPU supports
1. Run a benchmark:
    1. inside the container `docker exec embedding-calculator ./benchmark`
    1. or locally `cd .embedding-calculator && ./benchmark.sh` (require exposing API at localhost:3000)

# Troubleshooting

### Windows

##### While building container, crashes with error `: invalid option`

CRLF file endings may cause this. To fix, run `$ dos2unix *`.

##### Installing packages `requirements.txt` in a local environment crashes

Package *uWSGI* is not supported on Windows. Workaround is to temporarily delete the line with the package name from `requirements.txt` and install without it.

# Misc
Check that the component is in valid state: run tests, build container, start it
```
$ make
$ make up
```
Get project line counts per file type
```
$ which tokei >/dev/null || conda install -y -c conda-forge tokei && tokei --exclude srcext/
```
