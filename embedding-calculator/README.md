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

| Tag                    | Scanner     | Build arguments               | Comment                       |
|------------------------|-------------|-------------------------------|-------------------------------|
| :0.4.0 :latest         | Facenet2018 |                               |                               |
| :0.4.0-insightface     | InsightFace | SCANNER=InsightFace           |                               |
| :0.4.0-insightface-gpu | InsightFace | SCANNER=InsightFace GPU_IDX=0 | GPU_IDX - index of GPU-device |


##### Build
Builds container (also runs main tests during the build):
```
$ docker build -t embedding-calculator 
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

### InsightFace scanner backend

FaceNet is a default scanner backend. It can be changed to InsightFace through passing build args:
```
$ docker build -t embedding-calculator --build-arg SCANNER=InsightFace .
```

#### Pretrained models

InsightFace has few build in models:  
* detection models: `retinaface_r50_v1` (default), `retinaface_mnet025_v1`, `retinaface_mnet025_v2`
* recognition model - `arcface_r100_v1`

Changing models is performed by passing  `build-args`, e.g. `--build-arg DETECTION_MODEL=retinaface_mnet025_v1`.   
Pass `DETECTION_MODEL` for a detection model, `CALCULATION_MODEL` for a recognition model.

##### More pretrained models

Check more models in [Model Zoo](https://github.com/deepinsight/insightface/wiki/Model-Zoo#3-face-recognition-models). To use it follow these steps: 

1. download model and unpack it to `embedding-calculator/srcext/insightface/models/`
1. run build with passing model name, e.g. `--build-arg CALCULATION_MODEL=model-r34-amf`. 
  
#### NVidia GPU support

Build container with CUDA 10.1.
```
$ docker build -t cuda101-py37 -f gpu.Dockerfile .
$ docker build -t embedding-calculator-gpu --build-arg SCANNER=InsightFace --build-arg GPU_IDX=0  .
```

Install the nvidia-docker2 package and dependencies:
```
sudo apt-get update
sudo apt-get install -y nvidia-docker2
sudo systemctl restart docker
```

Run with enabled gpu
```
$ docker run -p 3000:3000 --gpus all embedding-calculator-gpu
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
