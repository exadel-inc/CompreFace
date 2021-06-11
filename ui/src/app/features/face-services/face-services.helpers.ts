/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

import { ImageSize } from '../../data/interfaces/image';

/**
 * Beautify the result JSON format
 *
 * @param value String
 * @returns string
 * @example
 * {
    "result": [
      {
        "box": {
          ...
        },
        "faces": [
          {
            ...
          }
        ]
      }
    ]
  }
  change to
  {
  "result": [ {
      "box": {
        ...
        },
      "faces": [ {
        ...
        } ]
    } ]
}
 */
export const resultRecognitionFormatter = (value: string): string => value.replace(/(\[\n\s+)/g, '[ ').replace(/(\s+\])/g, ' ]');

/**
 * Get file extension.
 *
 * @param file File.
 * @returns `string` File extension
 * @example getFileExtension(file) ==> 'jpeg'
 */
export const getFileExtension = (file: File): string =>
  file.name.slice((Math.max(0, file.name.lastIndexOf('.')) || Infinity) + 1).toLowerCase();

/**
 * Recalculate face coordinates according to canvas size (design).
 *
 * @param box Face coordinates from BE.
 * @param imageSize Size of image.
 * @param sizeToCalc Canvas size. (design size).
 */
export const recalculateFaceCoordinate = (box: any, imageSize: ImageSize, sizeToCalc: ImageSize) => {
  const divideWidth = imageSize.width / sizeToCalc.width;
  const divideHeight = imageSize.height / sizeToCalc.height;

  return {
    ...box,
    /* eslint-disable @typescript-eslint/naming-convention */
    x_max: box.x_max / divideWidth > sizeToCalc.width ? sizeToCalc.width : box.x_max / divideWidth,
    x_min: box.x_min / divideWidth,
    y_max: box.y_max / divideHeight > sizeToCalc.height ? sizeToCalc.height : box.y_max / divideHeight,
    y_min: box.y_min / divideHeight,
    /* eslint-enable @typescript-eslint/naming-convention */
  };
};

/**
 * Recalculate face coordinates landmarks to canvas size (design).
 *
 * @param landmarks Landmarks coordinates from BE.
 * @param imageSize Size of image.
 * @param sizeToCalc Size of image.
 */
export const recalculateLandmarks = (landmarks: [number[]], imageSize: ImageSize, sizeToCalc: ImageSize) => {
  const divideWidth = imageSize.width / sizeToCalc.width;
  const divideHeight = imageSize.height / sizeToCalc.height;

  return landmarks.map(val => [
    val[0] / divideWidth > sizeToCalc.width ? sizeToCalc.width : val[0] / divideWidth,
    val[1] / divideHeight > sizeToCalc.height ? sizeToCalc.height : val[1] / divideHeight,
  ]);
};
