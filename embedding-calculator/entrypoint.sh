#!/bin/sh

if [ "$RECOGNITION_OPTIMIZATION" = "intel_mkl" ]; \
    then export GPU_IDX="$GPU_IDX" INTEL_OPTIMIZATION="true"; \
    else export GPU_IDX="-1" INTEL_OPTIMIZATION="false"; \
fi

uwsgi --ini uwsgi.ini