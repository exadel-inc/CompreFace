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

import { Observable } from 'rxjs';

export interface ImageSize {
  width: any;
  height: any;
}

/**
 * Get image size.
 *
 * @param file File.
 */
export function getImageSize(file: File): Observable<ImageSize> {
  return new Observable(subscriber => {
    const img = new Image();
    img.onload = () => {
      subscriber.next({ width: img.width, height: img.height });
      subscriber.complete();
    };
    img.src = URL.createObjectURL(file);
  });
}

/**
 * Recalculate face coordinates according to canvas size (design).
 *
 * @param box Face coordinates from BE.
 * @param imageSize Size of image.
 * @param sizeToCalc Canvas size. (design size).
 * @param yAxisPadding padding to ensure capacity for text area on image.
 */
export function recalculateFaceCoordinate(box: any, imageSize: ImageSize, sizeToCalc: ImageSize, yAxisPadding: number): any {
  const divideWidth = imageSize.width / sizeToCalc.width;
  const divideHeight = imageSize.height / sizeToCalc.height;

  return {
    ...box,
    x_max: box.x_max / divideWidth > sizeToCalc.width ? sizeToCalc.width : box.x_max / divideWidth,
    x_min: box.x_min / divideWidth,
    y_max: box.y_max / divideHeight > sizeToCalc.height - yAxisPadding ? sizeToCalc.height - yAxisPadding : box.y_max / divideHeight,
    y_min: box.y_min / divideHeight > yAxisPadding ? box.y_min / divideHeight : yAxisPadding,
  };
}
