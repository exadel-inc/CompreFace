SHELL := /bin/bash
.DEFAULT_GOAL := default
.PHONY := default up

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
	docker build -t embedding-calculator --build-arg SCANNER=Facenet2018,InsightFace .

up:
	docker run -p3000:3000 embedding-calculator

tools/tmp:
	$(call get_from_remote_zip,https://www.fontsquirrel.com/fonts/download/arimo,tools/tmp/arimo-font)

tools/benchmark_detection/tmp:
	$(call get_from_remote_tgz,http://tamaraberg.com/faceDataset/originalPics.tar.gz,tools/benchmark_detection/tmp/originalPics)
	$(call get_from_remote_tgz,http://vis-www.cs.umass.edu/fddb/FDDB-folds.tgz,tools/benchmark_detection/tmp)
