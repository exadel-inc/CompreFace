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
  return new Observable((subscriber) => {
    const img = new Image();
    img.onload = () => {
      subscriber.next({width: img.width, height: img.height});
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
 */
export function recalculateFaceCoordinate(box: any, imageSize: ImageSize, sizeToCalc: ImageSize): any {
  const divideWidth = imageSize.width / sizeToCalc.width;
  const divideHeight = imageSize.height / sizeToCalc.height;

  return {
    ...box,
    x_max: box.x_max / divideWidth,
    x_min: box.x_min / divideWidth,
    y_max: box.y_max / divideHeight,
    y_min: box.y_min / divideHeight
  };
}
