![Example output image](./sample_images/readme_example.png)

# embedding-calculator
This is a component of Exadel Face Recognition Service. EFRS is a service for face recognition: upload images with faces of known people, then upload a new image, and the service will recognize faces in it.

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
Builds, tests, and runs a container:
```
$ docker build -t embedding-calculator .
$ docker run -p3000:3000 embedding-calculator
```
To skip tests during build use:
```
$ docker build -t embedding-calculator --build-arg SKIP_TESTS=true .
```

### Run tests
Unit tests
```
$ pytest -m "not integration" src tools
```
Integration tests
```
$ pytest -m integration src tools
```
Lint checks
```
$ python -m pylama --options pylama.ini src tools
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

# Troubleshooting

### Windows

##### While building container, crashes with error `: invalid option`

CRLF file endings may cause this. To fix, run `$ dos2unix *`.

##### Installing packages `requirements.txt` in a local environment crashes

Package *uWSGI* is not supported on Windows. Workaround is to temporarily delete the line with the package name from `requirements.txt` and install without it.

# Misc
Get project line counts per file type
```
$ which tokei >/dev/null || conda install -y -c conda-forge tokei && tokei --exclude srcext/
```
