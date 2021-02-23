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

import { Component, ElementRef, Input, OnDestroy, ViewChild, OnChanges, SimpleChanges, Output, EventEmitter } from '@angular/core';
import {from, Observable, Subscription} from 'rxjs';
import { map, tap } from 'rxjs/operators';
import {
  getImageSize,
  ImageSize,
  recalculateFaceCoordinate,
  resultRecognitionFormatter,
  createDefaultImage,
} from '../../face-services.helpers';
import { RequestResult } from '../../../../data/interfaces/response-result';
import { RequestInfo } from '../../../../data/interfaces/request-info';
import {VerificationServiceFields} from '../../../../data/enums/verification-service.enum';

@Component({
  selector: 'app-verification-result',
  templateUrl: './verification-result.component.html',
  styleUrls: ['./verification-result.component.scss'],
})
export class VerificationResultComponent implements OnChanges, OnDestroy {
  @Input() processFile: File;
  @Input() checkFile: File;
  @Input() requestInfo: RequestInfo;
  @Input() printData: RequestResult;
  @Input() files: Observable<any>;
  @Input() isLoaded: boolean;
  @Input() pending: boolean;
  @Output() selectProcessFile = new EventEmitter();
  @Output() selectCheckFile = new EventEmitter();

  @ViewChild('processFileCanvasElement') set processFileCanvasElement(canvas: ElementRef) {
    if (canvas) {
      if (this.printSubscription1) {
        this.printSubscription1.unsubscribe();
      }
      if (this.processFile) {
        this.printSubscription1 = this.printResult(
          canvas,
          this.processFileCanvasSize,
          this.processFile,
          this.printData,
          VerificationServiceFields.processFileData
        ).subscribe();
      }
    }
  }

  @ViewChild('checkFileCanvasElement') set checkFileCanvasElement(canvas: ElementRef) {
    if (canvas) {
      if (this.printSubscription2) {
        this.printSubscription2.unsubscribe();
      }
      if (this.checkFile) {
        this.printSubscription2 = this.printResult(
          canvas,
          this.checkFileCanvasSize,
          this.checkFile,
          this.printData,
          VerificationServiceFields.checkFileData
        ).subscribe();
      }
    }
  }

  processFileCanvasSize: ImageSize = { width: 500, height: null };
  checkFileCanvasSize: ImageSize = { width: 500, height: null };
  faceDescriptionHeight = 25;
  formattedResult: string;

  private printSubscription1: Subscription;
  private printSubscription2: Subscription;

  ngOnChanges(changes: SimpleChanges) {
    if (changes?.requestInfo?.currentValue) {
      this.formattedResult = resultRecognitionFormatter(this.requestInfo.response);
    }
  }

  ngOnDestroy() {
    if (this.printSubscription1) {
      this.printSubscription1.unsubscribe();
    }
    if (this.printSubscription2) {
      this.printSubscription2.unsubscribe();
    }
  }

  printResult(canvas: ElementRef, canvasSize: any, file: any, data, key: string): Observable<any> {
    return getImageSize(file).pipe(
      tap(({ width, height }) => {
        canvasSize.height = (height / width) * canvasSize.width;
        canvas.nativeElement.setAttribute('height', canvasSize.height);
      }),
      map(imageSize => this.prepareForDraw(imageSize, data, canvasSize, key)),
      map(preparedImageData => this.drawCanvas(canvas, preparedImageData, file, canvasSize))
    );
  }

  private prepareForDraw(size, rawData, canvasSize, key): Observable<any> {
    return (
      rawData &&
      rawData.map(value => ({
        box: recalculateFaceCoordinate(value[key].box, size, canvasSize, this.faceDescriptionHeight),
        similarity: value.similarity,
      }))
    );
  }

  private createVerificationImage(ctx, box, face) {
    ctx = createDefaultImage(ctx, box);
    ctx.fillStyle = 'green';
    ctx.fillRect(box.x_min, box.y_max, box.x_max - box.x_min, this.faceDescriptionHeight);
    ctx.fillStyle = 'white';
    ctx.fillText(face.similarity, box.x_min + 10, box.y_max + 20);
  }

  /*
   * Make canvas and draw facanvasSizece and info on image.
   *
   * @preparedData prepared box data and faces.
   */
  drawCanvas(canvas, data, file, canvasSize) {
    this.createImage(canvas, file, canvasSize, (img, ctx) => {
      if (!data) return;
      for (const value of data) {
        // eslint-disable-next-line @typescript-eslint/naming-convention
        this.createVerificationImage(ctx, value.box, value);
      }
    });
  }

  createImage(canvas, file, canvasSize, draw) {
    const img = new Image();
    const ctx: CanvasRenderingContext2D = canvas.nativeElement.getContext('2d');
    img.onload = () => {
      ctx.drawImage(img, 0, 0, canvasSize.width, canvasSize.height);
      draw(img, ctx);
    };
    img.src = URL.createObjectURL(file);
  }
}
