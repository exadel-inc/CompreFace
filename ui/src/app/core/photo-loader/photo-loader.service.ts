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
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { map, switchMap } from 'rxjs/operators';
import { Observable } from 'rxjs';

import { decode } from 'tiff';
import TiffIfd from 'tiff/lib/tiffIfd';

@Injectable({
  providedIn: 'root',
})
export class LoadingPhotoService {
  private type: string[] = [
    'image/bmp',
    'image/gif',
    'image/jpeg',
    'image/png',
    'image/tiff',
    'image/vnd.wap.wbmp',
    'image/webp',
    'image/x-icon',
    'image/x-jng',
  ];

  get imageType(): string[] {
    return this.type;
  }

  constructor(private http: HttpClient) {}

  tiffConvertor(url: string): Observable<ImageBitmap> {
    return this.http.get(url, { responseType: 'arraybuffer' }).pipe(
      map((array: ArrayBuffer) => {
        const ifd: TiffIfd = decode(array)[0];
        const imageData: ImageData = new ImageData(ifd.width, ifd.height);

        for (let i = 0; i < ifd.data.length; i++) {
          imageData.data[i] = ifd.data[i];
        }

        return imageData as ImageData;
      }),
      switchMap(async (imageData: ImageData) => (await createImageBitmap(imageData)) as ImageBitmap)
    );
  }

  createImage(url: string): Observable<ImageBitmap> {
    return this.http
      .get(url, { responseType: 'blob' })
      .pipe(switchMap(async (blob: Blob) => (await createImageBitmap(blob)) as ImageBitmap));
  }

  loader(file: File): Observable<ImageBitmap> {
    const checkImageType: boolean = this.imageType.includes(file.type);

    if (!checkImageType) return;

    const url: string = URL.createObjectURL(file);
    const type = 'image/tiff';

    return file.type === type ? this.tiffConvertor(url) : this.createImage(url);
  }
}
