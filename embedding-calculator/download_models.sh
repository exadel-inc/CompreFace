#!/bin/bash -e

if [[ "$SCANNER" == "InsightFace" ]]; then
  for MODEL in $DETECTION_MODEL $CALCULATION_MODEL
  do
    URL=http://insightface.ai/files/models/$MODEL.zip
    echo "Downloading $URL..."
    mkdir -p ~/.insightface/models/$MODEL && cd "$_" && curl -L $URL -o m.zip  \
      && unzip m.zip && rm m.zip
  done
fi