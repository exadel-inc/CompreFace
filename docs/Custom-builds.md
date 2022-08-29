# Custom Builds

There is always a trade-off between the face recognition accuracy, the
system's max throughput, and even hardware support.

By default, the CompreFace release contains configuration that could be
run on the widest variety of hardware.

The downside of this build is that it's not optimized for the latest
generations of CPU and doesn't support GPU.

With custom-builds, we aim to cover as many cases as we can. They are
not tested as well as the default build, and we encourage the community
to report any bugs related to these builds.

## List of custom-builds

You can find the list of custom builds [here](../custom-builds/README.md)

## Contribution

We also encourage the community to share their builds; we will add them
to our list with notice that this is a community build.

## How to choose a build

Different builds are fit for different purposes - some of them have
higher accuracy, but the performance on CPU is low; others are optimized
for low-performance hardware and have acceptable accuracy. Of course,
you have to make your own choice in this trade-off. But generally, you
can follow these rules:

-   If you want to run real-time face recognition, we recommend choosing
    builds with GPU support.
-   If you need to run face recognition on old or low-performance
    systems, we recommend using builds with models initially created for
    mobile
-   Do not take the most accurate model blindly. The accuracy does not
    vary significantly between models, but the required hardware
    resources could differ dramatically

## How to run custom-builds

Running custom-build is very similar to running the default build - all
you need to do is open the corresponding folder and run
`docker-compose up -d`.

Things to consider:

- If you run CompreFace from the custom-build
  folder, it creates a new docker volume so that you won't see your saved
  information. To run custom-build with your previously saved information,
  you need to copy files from custom-build to folder with the original
  build (and replace the original files)
- In most cases, face recognition
  models are not interchangeable; this means that all you saved examples
  from the old build won't work on new builds. See [migrations
  documentation](Face-data-migration.md) to know what is the options.
- Do not run two instances of CompreFace simultaneously without changing the
  port. To change the port, go to the `docker-compose` file and change the
  post for the `compreface-fe` container.

## How to build your own custom-build

### Custom models

CompreFace supports two face recognition libraries - FaceNet and
InsightFace. It means CompreFace can run any model that can run these
libraries. So all you need to do is 
1. Upload your model to Google Drive and add it to one of the following files into the `Calculator` class:
   - /embedding-calculator/src/services/facescan/plugins/facenet/facenet.py
   - /embedding-calculator/src/services/facescan/plugins/insightface/insightface.py
2. Take the `docker-compose` file from `/dev` folder as a template 
3. Specify new model names in build arguments. For more information, look
   at [this documentation](https://github.com/exadel-inc/CompreFace/tree/master/embedding-calculator#run-service). E.g. here is a part of the
   `docker-compose` file for building with a custom model with GPU support:
```yaml
compreface-core:
  image: ${registry}compreface-core:${CORE_VERSION}
  container_name: "compreface-core"
  ports:
    - "3300:3000"
  runtime: nvidia
  build:
    context: ../embedding-calculator
    args:
      - FACE_DETECTION_PLUGIN=insightface.FaceDetector@retinaface_r50_v1
      - CALCULATION_PLUGIN=insightface.Calculator@arcface_r100_v1
      - EXTRA_PLUGINS=insightface.LandmarksDetector,insightface.GenderDetector,insightface.AgeDetector,insightface.facemask.MaskDetector
      - BASE_IMAGE=compreface-core-base:base-cuda100-py37
      - GPU_IDX=0
  environment:
    - ML_PORT=3000
```