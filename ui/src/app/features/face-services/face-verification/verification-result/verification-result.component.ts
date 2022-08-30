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
import { Component, Input, OnChanges, Output, EventEmitter, SimpleChanges, ChangeDetectionStrategy } from '@angular/core';

import { RequestInfo } from '../../../../data/interfaces/request-info';
import { ServiceTypes } from '../../../../data/enums/service-types.enum';
import { ImageConvert } from '../../../../data/interfaces/image-convert';

import { recalculateFaceCoordinate, recalculateLandmarks, resultRecognitionFormatter } from '../../face-services.helpers';
import { SourceImageFace } from '../../../../data/interfaces/source-image-face';
import { FaceMatches } from '../../../../data/interfaces/face-matches';

@Component({
  selector: 'app-verification-result',
  templateUrl: './verification-result.component.html',
  styleUrls: ['./verification-result.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VerificationResultComponent implements OnChanges {
  @Input() set processPhoto(image: ImageBitmap) {
    this.resizeProcessPhoto = this.resize(image);
  }
  @Input() set processPhotoData(data: SourceImageFace[]) {
    this.printDataProcessPhoto = this.recalculate(data, this.resizeProcessPhoto) as SourceImageFace[];
  }
  @Input() set checkPhoto(image: ImageBitmap) {
    this.resizeCheckPhoto = this.resize(image);
  }
  @Input() set checkPhotoData(data: FaceMatches[]) {
    this.printDataCheckPhoto = this.recalculate(data, this.resizeCheckPhoto) as FaceMatches[];
  }

  @Input() requestInfo: RequestInfo;
  @Input() isLoaded: boolean;
  @Input() pending: boolean;
  @Input() type: ServiceTypes;
  @Input() maxImageSize: number;

  @Output() selectProcessFile = new EventEmitter();
  @Output() selectCheckFile = new EventEmitter();
  @Output() resetProcessFile = new EventEmitter();
  @Output() resetCheckFile = new EventEmitter();

  widthCanvas = 500;
  formattedResult: string;

  resizeProcessPhoto: ImageConvert;
  resizeCheckPhoto: ImageConvert;

  printDataProcessPhoto: SourceImageFace[];
  printDataCheckPhoto: FaceMatches[];

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

  recalculate(data: any[], image: ImageConvert): SourceImageFace[] | FaceMatches[] {
    return !!data
      ? (data.map(val => ({
          ...val,
          box: recalculateFaceCoordinate(val.box, image.imageBitmap, image.sizeCanvas),
          landmarks: recalculateLandmarks(val.landmarks, image.imageBitmap, image.sizeCanvas),
        })) as SourceImageFace[] | FaceMatches[])
      : null;
  }

  onResetProcessFile(event?: File): void {
    this.resetProcessFile.emit();
    if (event) this.resetProcessFile.emit(event);
  }

  onResetCheckFile(event?: File): void {
    this.resetCheckFile.emit();
    if (event) this.resetCheckFile.emit(event);
  }
}
