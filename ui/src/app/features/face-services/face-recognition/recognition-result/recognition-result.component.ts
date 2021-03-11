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

import { Component, ElementRef, Input, ViewChild, OnChanges, SimpleChanges } from '@angular/core';
import { Observable } from 'rxjs';
import { first, map, tap } from 'rxjs/operators';

import { ServiceTypes } from '../../../../data/enums/service-types.enum';
import { recalculateFaceCoordinate, resultRecognitionFormatter, createDefaultImage } from '../../face-services.helpers';
import { RequestResult } from '../../../../data/interfaces/response-result';
import { RequestInfo } from '../../../../data/interfaces/request-info';
import { LoadingPhotoService } from '../../../../core/photo-loader/photo-loader.service';
import { ImageSize } from '../../../../data/interfaces/image';

@Component({
  selector: 'app-recognition-result',
  templateUrl: './recognition-result.component.html',
  styleUrls: ['./recognition-result.component.scss'],
})
export class RecognitionResultComponent implements OnChanges {
  @Input() file: File;
  @Input() requestInfo: RequestInfo;
  @Input() printData: RequestResult;
  @Input() isLoaded: boolean;
  @Input() type: string;

  @ViewChild('canvasElement') set canvasElement(canvas: ElementRef) {
    if (canvas) {
      this.myCanvas = canvas;

      if (this.printData && this.myCanvas) {
        this.printResult(this.printData).pipe(first()).subscribe();
      }
    }
  }

  canvasSize: ImageSize = { width: 500, height: null };
  myCanvas: ElementRef;
  faceDescriptionHeight = 25;
  formattedResult: string;
  private imgCanvas: ImageBitmap;

  constructor(private loadingPhotoService: LoadingPhotoService) {}

  ngOnChanges(changes: SimpleChanges) {
    if (changes?.requestInfo?.currentValue) {
      this.formattedResult = resultRecognitionFormatter(this.requestInfo.response);
    }
  }

  printResult(result: any): Observable<any> {
    return this.loadingPhotoService.loader(this.file).pipe(
      tap((bitmap: ImageBitmap) => {
        this.canvasSize.height = (bitmap.height / bitmap.width) * this.canvasSize.width;
        this.myCanvas.nativeElement.setAttribute('height', this.canvasSize.height);
        this.imgCanvas = bitmap;
      }),
      map(imageSize => this.prepareForDraw(imageSize, result)),
      map(preparedImageData => this.drawCanvas(preparedImageData))
    );
  }

  private prepareForDraw(size, rawData): Observable<any> {
    return rawData.map(value => ({
      box: recalculateFaceCoordinate(value.box, size, this.canvasSize, this.faceDescriptionHeight),
      subjects: value.subjects,
    }));
  }

  private createRecognitionImage(ctx, box, face) {
    ctx = createDefaultImage(ctx, box);
    ctx.fillStyle = 'green';
    ctx.fillRect(box.x_min, box.y_min - this.faceDescriptionHeight, box.x_max - box.x_min, this.faceDescriptionHeight);
    ctx.fillRect(box.x_min, box.y_max, box.x_max - box.x_min, this.faceDescriptionHeight);
    ctx.fillStyle = 'white';
    ctx.fillText(face.similarity, box.x_min + 10, box.y_max + 20);
    ctx.fillText(face.subject, box.x_min + 10, box.y_min - 5);
  }

  private createDetectionImage(ctx, box) {
    ctx = createDefaultImage(ctx, box);
    ctx.fillStyle = 'green';
    ctx.fillRect(box.x_min, box.y_max, box.x_max - box.x_min, this.faceDescriptionHeight);
    ctx.fillStyle = 'white';
    ctx.fillText(box.probability.toFixed(4), box.x_min + 10, box.y_max + 20);
  }

  /*
   * Make canvas and draw face and info on image.
   *
   * @preparedData prepared box data and subjects.
   */
  drawCanvas(preparedData) {
    switch (this.type) {
      case ServiceTypes.Recognition:
        this.drawRecognitionCanvas(preparedData);
        break;

      case ServiceTypes.Detection:
        this.drawDetectionCanvas(preparedData);
        break;
    }
  }

  createImage(draw) {
    const ctx: CanvasRenderingContext2D = this.myCanvas.nativeElement.getContext('2d');
    ctx.drawImage(this.imgCanvas, 0, 0, this.canvasSize.width, this.canvasSize.height);
    draw(ctx);
  }

  drawRecognitionCanvas(data) {
    this.createImage(ctx => {
      for (const value of data) {
        // eslint-disable-next-line @typescript-eslint/naming-convention
        const resultFace = value.subjects.length > 0 ? value.subjects[0] : { subject: undefined, similarity: 0 };
        this.createRecognitionImage(ctx, value.box, resultFace);
      }
    });
  }

  drawDetectionCanvas(data) {
    this.createImage(ctx => {
      for (const value of data) {
        // eslint-disable-next-line @typescript-eslint/naming-convention
        this.createDetectionImage(ctx, value.box);
      }
    });
  }
}
