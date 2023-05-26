SHELL := /bin/bash
.DEFAULT_GOAL := default
.PHONY := default up build-images build-cuda

IMAGE := ${DOCKER_REGISTRY}compreface-core
CUDA_IMAGE := $(IMAGE)-base:base-cuda118-py38
APPERY_ARG := --build-arg APPERY_API_KEY=${APPERY_API_KEY}

MOBILENET_BUILD_ARGS := --build-arg FACE_DETECTION_PLUGIN=insightface.FaceDetector@retinaface_mnet025_v1 \
                          --build-arg CALCULATION_PLUGIN=insightface.Calculator@arcface_mobilefacenet \
                          --build-arg EXTRA_PLUGINS=insightface.LandmarksDetector,insightface.GenderDetector,insightface.AgeDetector,insightface.facemask.MaskDetector,insightface.PoseEstimator \
                          $(APPERY_ARG)

ARCFACE_r100_BUILD_ARGS := --build-arg FACE_DETECTION_PLUGIN=insightface.FaceDetector@retinaface_r50_v1 \
                          --build-arg CALCULATION_PLUGIN=insightface.Calculator@arcface-r100-msfdrop75 \
                          --build-arg EXTRA_PLUGINS=insightface.LandmarksDetector,insightface.GenderDetector,insightface.AgeDetector,insightface.facemask.MaskDetector,insightface.PoseEstimator \
                          $(APPERY_ARG)

FACENET_BUILD_ARGS := --build-arg FACE_DETECTION_PLUGIN=facenet.FaceDetector \
                          --build-arg CALCULATION_PLUGIN=facenet.Calculator \
                          --build-arg EXTRA_PLUGINS=facenet.LandmarksDetector,agegender.GenderDetector,agegender.AgeDetector,facenet.facemask.MaskDetector,facenet.PoseEstimator \
                          $(APPERY_ARG)

GPU_ARGS := --build-arg GPU_IDX=0 --build-arg BASE_IMAGE=$(CUDA_IMAGE)

define get_from_remote_tgz
	mkdir -p $(2)
	curl -o $(2)/tmp.tgz $(1)
	tar zxvf $(2)/tmp.tgz -C $(2)
	rm $(2)/tmp.tgz
endef

define get_from_remote_zip
	mkdir -p $(2)
	curl -o $(2)/tmp.zip $(1)
	unzip $(2)/tmp.zip -d $(2)
	rm $(2)/tmp.zip
endef

default:
	pytest src tools
	python -m pylama --options pylama.ini src tools
	docker build . -t $(IMAGE):$(VERSION)

build-cuda:
	docker build . --file gpu.Dockerfile --tag $(CUDA_IMAGE)

build-images: build-cuda
	docker build . -t $(IMAGE):$(VERSION)-facenet $(FACENET_BUILD_ARGS)
	docker build . -t $(IMAGE):$(VERSION)-mobilenet $(MOBILENET_BUILD_ARGS)
	docker build . -t $(IMAGE):$(VERSION)-mobilenet-gpu $(MOBILENET_BUILD_ARGS) $(GPU_ARGS)
	docker build . -t $(IMAGE):$(VERSION)-arcface-r100 $(ARCFACE_r100_BUILD_ARGS)
	docker build . -t $(IMAGE):$(VERSION)-arcface-r100-gpu $(ARCFACE_r100_BUILD_ARGS) $(GPU_ARGS)

build-images-cpu:
	docker build . -t $(IMAGE):$(VERSION)-facenet $(FACENET_BUILD_ARGS)
	docker build . -t $(IMAGE):$(VERSION)-mobilenet $(MOBILENET_BUILD_ARGS)
	docker build . -t $(IMAGE):$(VERSION)-arcface-r100 $(ARCFACE_r100_BUILD_ARGS)

build-images-gpu: build-cuda
	docker build . -t $(IMAGE):$(VERSION)mobilenet-gpu $(MOBILENET_BUILD_ARGS) $(GPU_ARGS)
	docker build . -t $(IMAGE):$(VERSION)arcface-r100-gpu $(ARCFACE_r100_BUILD_ARGS) $(GPU_ARGS)

up:
	docker run -p3000:3000 embedding-calculator

tools/tmp:
	$(call get_from_remote_zip,https://www.fontsquirrel.com/fonts/download/arimo,tools/tmp/arimo-font)

tools/benchmark_detection/tmp:
	$(call get_from_remote_tgz,http://tamaraberg.com/faceDataset/originalPics.tar.gz,tools/benchmark_detection/tmp/originalPics)
	$(call get_from_remote_tgz,http://vis-www.cs.umass.edu/fddb/FDDB-folds.tgz,tools/benchmark_detection/tmp)
	
