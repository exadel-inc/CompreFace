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
import { Component, Input, OnChanges, SimpleChanges, Output, EventEmitter, ChangeDetectionStrategy } from '@angular/core';

import { RequestInfo } from '../../../../data/interfaces/request-info';
import { ServiceTypes } from '../../../../data/enums/service-types.enum';
import { ImageConvert } from '../../../../data/interfaces/image-convert';
import { RequestResultRecognition } from '../../../../data/interfaces/request-result-recognition';
import { recalculateFaceCoordinate, recalculateLandmarks, resultRecognitionFormatter } from '../../face-services.helpers';

@Component({
  selector: 'app-recognition-result',
  templateUrl: './recognition-result.component.html',
  styleUrls: ['./recognition-result.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RecognitionResultComponent implements OnChanges {
  @Input() set photo(image: ImageBitmap) {
    this.resizePhoto = this.resize(image);
  }

  @Input() set printData(data: RequestResultRecognition[]) {
    this.recalculatePrintData = this.recalculate(data, this.resizePhoto);
  }

  @Input() requestInfo: RequestInfo;
  @Input() isLoaded: boolean;
  @Input() pending: boolean;
  @Input() type: ServiceTypes;
  @Input() maxImageSize: number;

  @Output() selectFile = new EventEmitter();
  @Output() resetFace = new EventEmitter();

  widthCanvas = 500;
  formattedResult: string;
  resizePhoto: ImageConvert;
  recalculatePrintData: RequestResultRecognition[];

  ngOnChanges(changes: SimpleChanges): void {
    if (!!this.requestInfo) this.formattedResult = resultRecognitionFormatter(this.requestInfo.response);
  }

  resize(image: ImageBitmap): ImageConvert {
    return !!image
      ? ({
          imageBitmap: image,
          sizeCanvas: { width: this.widthCanvas, height: (image.height / image.width) * this.widthCanvas },
        } as ImageConvert)
      : null;
  }

  recalculate(data: RequestResultRecognition[], image: ImageConvert): RequestResultRecognition[] {
    return !!data
      ? (data.map(val => ({
          ...val,
          box: recalculateFaceCoordinate(val.box, image.imageBitmap, image.sizeCanvas),
          landmarks: recalculateLandmarks(val.landmarks, image.imageBitmap, image.sizeCanvas),
        })) as RequestResultRecognition[])
      : null;
  }

  onResetFile(event?: File): void {
    this.resetFace.emit();
    if (event) this.selectFile.emit(event);
  }
}
