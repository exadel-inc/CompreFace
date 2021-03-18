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

import { Component, ElementRef, Input, ViewChild, OnChanges, SimpleChanges, Output, EventEmitter } from '@angular/core';
import { Observable } from 'rxjs';
import { first, map, tap } from 'rxjs/operators';
import { recalculateFaceCoordinate, resultRecognitionFormatter, createDefaultImage } from '../../face-services.helpers';
import { RequestResult } from '../../../../data/interfaces/response-result';
import { RequestInfo } from '../../../../data/interfaces/request-info';
import { VerificationServiceFields } from '../../../../data/enums/verification-service.enum';
import { LoadingPhotoService } from '../../../../core/photo-loader/photo-loader.service';
import { ImageSize } from '../../../../data/interfaces/image';

@Component({
  selector: 'app-verification-result',
  templateUrl: './verification-result.component.html',
  styleUrls: ['./verification-result.component.scss'],
})
export class VerificationResultComponent implements OnChanges {
  @Input() requestInfo: RequestInfo;
  @Input() printData: RequestResult;
  @Input() files: any;
  @Input() isLoaded: boolean;
  @Input() pending: boolean;
  @Output() selectProcessFile = new EventEmitter();
  @Output() selectCheckFile = new EventEmitter();

  @ViewChild('processFileCanvasElement') set processFileCanvasElement(canvas: ElementRef) {
    this.processFileCanvasLink = canvas;
  }

  @ViewChild('checkFileCanvasElement') set checkFileCanvasElement(canvas: ElementRef) {
    this.checkFileCanvasLink = canvas;
  }

  processFileCanvasSize: ImageSize = { width: 500, height: null };
  checkFileCanvasSize: ImageSize = { width: 500, height: null };
  faceDescriptionHeight = 25;
  formattedResult: string;

  private processFileCanvasLink: any = null;
  private checkFileCanvasLink: any = null;
  private imgCanvas: ImageBitmap;

  constructor(private loadingPhotoService: LoadingPhotoService) {}

  ngOnChanges(changes: SimpleChanges) {
    if (changes?.requestInfo?.currentValue) {
      this.formattedResult = resultRecognitionFormatter(this.requestInfo.response);
    }

    if (changes?.files?.currentValue) {
      if (changes.files.currentValue.checkFile) {
        this.refreshCanvas(
          this.checkFileCanvasLink,
          this.checkFileCanvasSize,
          changes.files.currentValue.checkFile,
          this.printData,
          VerificationServiceFields.checkFileData
        );
      }
    }

    if (changes?.files?.currentValue) {
      if (changes.files.currentValue.processFile) {
        this.refreshCanvas(
          this.processFileCanvasLink,
          this.processFileCanvasSize,
          changes.files.currentValue.processFile,
          this.printData,
          VerificationServiceFields.processFileData
        );
      }
    }
  }

  printResult(canvas: ElementRef, canvasSize: any, file: any, data, key: string): Observable<any> {
    return this.loadingPhotoService.loader(file).pipe(
      tap((bitmap: ImageBitmap) => {
        canvasSize.height = (bitmap.height / bitmap.width) * canvasSize.width;
        canvas.nativeElement.setAttribute('height', canvasSize.height);
        this.imgCanvas = bitmap;
      }),
      map(imageSize => this.prepareForDraw(imageSize, data, canvasSize, key)),
      map(preparedImageData => this.drawCanvas(canvas, preparedImageData, file, canvasSize))
    );
  }

  private refreshCanvas(canvas, canvasSize, file, data, field) {
    this.printResult(canvas, canvasSize, file, data, field).pipe(first()).subscribe();
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
    this.createImage(canvas, file, canvasSize, ctx => {
      if (!data) return;
      for (const value of data) {
        // eslint-disable-next-line @typescript-eslint/naming-convention
        this.createVerificationImage(ctx, value.box, value);
      }
    });
  }

  createImage(canvas, file, canvasSize, draw) {
    const ctx: CanvasRenderingContext2D = canvas.nativeElement.getContext('2d');
    ctx.drawImage(this.imgCanvas, 0, 0, canvasSize.width, canvasSize.height);
    draw(ctx);
  }
}
