SHELL := /bin/bash
.DEFAULT_GOAL := default
.PHONY := default up build-images build-cuda

IMAGE := exadel/comrepafe-core
VERSION ?= dev
INSIGHTFACE_BUILD_ARGS := --build-arg FACE_DETECTION_PLUGIN=insightface.FaceDetector \
                          --build-arg CALCULATION_PLUGIN=insightface.Calculator \
                          --build-arg EXTRA_PLUGINS=insightface.LandmarksDetector

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
	docker build . --file gpu.Dockerfile --tag $(IMAGE):base-cuda100-py37

build-images: build-cuda
	docker build . -t $(IMAGE):$(VERSION)
	docker build . -t $(IMAGE):$(VERSION)-insightface $(INSIGHTFACE_BUILD_ARGS)
	docker build . -t $(IMAGE):$(VERSION)-insightface-gpu $(INSIGHTFACE_BUILD_ARGS) --build-arg GPU_IDX=0

up:
	docker run -p3000:3000 embedding-calculator

tools/tmp:
	$(call get_from_remote_zip,https://www.fontsquirrel.com/fonts/download/arimo,tools/tmp/arimo-font)

tools/benchmark_detection/tmp:
	$(call get_from_remote_tgz,http://tamaraberg.com/faceDataset/originalPics.tar.gz,tools/benchmark_detection/tmp/originalPics)
	$(call get_from_remote_tgz,http://vis-www.cs.umass.edu/fddb/FDDB-folds.tgz,tools/benchmark_detection/tmp)
