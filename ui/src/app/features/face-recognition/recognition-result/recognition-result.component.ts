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

import { Component, ElementRef, Input, OnDestroy, ViewChild, OnChanges, SimpleChanges } from '@angular/core';
import { Observable, Subscription } from 'rxjs';
import { map, tap } from 'rxjs/operators';

import { ServiceTypes } from '../../../data/enums/model.enum';
import { getImageSize, ImageSize, recalculateFaceCoordinate, resultRecognitionFormatter } from '../face-recognition.helpers';
import { RequestResult } from '../../../data/interfaces/response-result';
import { RequestInfo } from '../../../data/interfaces/request-info';

@Component({
  selector: 'app-recognition-result',
  templateUrl: './recognition-result.component.html',
  styleUrls: ['./recognition-result.component.scss'],
})
export class RecognitionResultComponent implements OnChanges, OnDestroy {
  @Input() file: File;
  @Input() requestInfo: RequestInfo;
  @Input() printData: RequestResult;
  @Input() isLoaded: boolean;
  @Input() type: string;

  @ViewChild('canvasElement') set canvasElement(canvas: ElementRef) {
    if (canvas) {
      this.myCanvas = canvas;

      if (this.printSubscription) {
        this.printSubscription.unsubscribe();
      }

      if (this.printData && this.myCanvas) {
        this.printSubscription = this.printResult(this.printData).subscribe();
      }
    }
  }

  canvasSize: ImageSize = { width: 500, height: null };
  myCanvas: ElementRef;
  faceDescriptionHeight = 25;
  formattedResult: string;

  private printSubscription: Subscription;

  ngOnChanges(changes: SimpleChanges) {
    if (changes?.requestInfo?.currentValue) {
      this.formattedResult = resultRecognitionFormatter(this.requestInfo.response);
    }
  }

  ngOnDestroy() {
    if (this.printSubscription) {
      this.printSubscription.unsubscribe();
    }
  }

  printResult(result: any): Observable<any> {
    return getImageSize(this.file).pipe(
      tap(({ width, height }) => {
        this.canvasSize.height = (height / width) * this.canvasSize.width;
        this.myCanvas.nativeElement.setAttribute('height', this.canvasSize.height);
      }),
      map(imageSize => this.prepareForDraw(imageSize, result)),
      map(preparedImageData => this.drawCanvas(preparedImageData))
    );
  }

  private prepareForDraw(size, rawData): Observable<any> {
    return rawData.map(value => ({
      box: recalculateFaceCoordinate(value.box, size, this.canvasSize, this.faceDescriptionHeight),
      faces: value.faces,
    }));
  }

  private createRecognitionImage(ctx, box, face) {
    ctx = this.createDefaultImage(ctx, box);
    ctx.fillStyle = 'green';
    ctx.fillRect(box.x_min, box.y_min - this.faceDescriptionHeight, box.x_max - box.x_min, this.faceDescriptionHeight);
    ctx.fillRect(box.x_min, box.y_max, box.x_max - box.x_min, this.faceDescriptionHeight);
    ctx.fillStyle = 'white';
    ctx.fillText(face.similarity, box.x_min + 10, box.y_max + 20);
    ctx.fillText(face.face_name, box.x_min + 10, box.y_min - 5);
  }

  private createDetectionImage(ctx, box) {
    ctx = this.createDefaultImage(ctx, box);
    ctx.fillStyle = 'green';
    ctx.fillRect(box.x_min, box.y_max, box.x_max - box.x_min, this.faceDescriptionHeight);
    ctx.fillStyle = 'white';
    ctx.fillText(box.probability.toFixed(4), box.x_min + 10, box.y_max + 20);
  }

  private createDefaultImage(ctx, box) {
    ctx.beginPath();
    ctx.strokeStyle = 'green';
    ctx.moveTo(box.x_min, box.y_min);
    ctx.lineTo(box.x_max, box.y_min);
    ctx.lineTo(box.x_max, box.y_max);
    ctx.lineTo(box.x_min, box.y_max);
    ctx.lineTo(box.x_min, box.y_min);
    ctx.stroke();
    ctx.font = '12pt Roboto Regular Helvetica Neue sans-serif';

    return ctx;
  }

  /*
   * Make canvas and draw face and info on image.
   *
   * @preparedData prepared box data and faces.
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

  createImage(drow) {
    const img = new Image();
    const ctx: CanvasRenderingContext2D = this.myCanvas.nativeElement.getContext('2d');
    img.onload = () => {
      ctx.drawImage(img, 0, 0, this.canvasSize.width, this.canvasSize.height);
      drow(img, ctx);
    };
    img.src = URL.createObjectURL(this.file);
  }

  drawRecognitionCanvas(data) {
    this.createImage((img, ctx) => {
      for (const value of data) {
        // eslint-disable-next-line @typescript-eslint/naming-convention
        const resultFace = value.faces.length > 0 ? value.faces[0] : { face_name: undefined, similarity: 0 };
        this.createRecognitionImage(ctx, value.box, resultFace);
      }
    });
  }

  drawDetectionCanvas(data) {
    this.createImage((img, ctx) => {
      for (const value of data) {
        // eslint-disable-next-line @typescript-eslint/naming-convention
        this.createDetectionImage(ctx, value.box);
      }
    });
  }
}
