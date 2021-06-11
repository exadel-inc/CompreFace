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
import { ChangeDetectionStrategy, Component, ElementRef, Input, OnChanges, OnInit, ViewChild } from '@angular/core';

import { ServiceTypes } from '../../../data/enums/service-types.enum';
import { ImageConvert } from '../../../data/interfaces/image-convert';
import { RequestResultRecognition } from '../../../data/interfaces/request-result-recognition';
import { FaceMatches } from '../../../data/interfaces/face-matches';
import { SourceImageFace } from '../../../data/interfaces/source-image-face';

@Component({
  selector: 'app-face-picture',
  templateUrl: './face-picture.component.html',
  styleUrls: ['./face-picture.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FacePictureComponent implements OnChanges, OnInit {
  @Input() picture: ImageConvert;
  @Input() printData: RequestResultRecognition[] | FaceMatches[] | SourceImageFace[];
  @Input() isLoaded: boolean;
  @Input() type: ServiceTypes;
  @Input() set showLandmarks(value: boolean) {
    this.disableLandmarks = value;
  }

  @ViewChild('canvasPicture', { static: true }) canvasPicture: ElementRef<HTMLCanvasElement>;
  @ViewChild('canvasLandmarks', { static: true }) canvasLandmarks: ElementRef<HTMLCanvasElement>;

  types = ServiceTypes;
  disableLandmarks = false;

  private currentPicture: () => any;

  ngOnInit(): void {
    this.initCanvasSize();
    this.currentPicture = this.loadPicture(this.canvasPicture, this.picture, this.currentPicture);
  }

  ngOnChanges(): void {
    this.initCanvasSize();
    if (!!this.currentPicture) this.currentPicture = this.loadPicture(this.canvasPicture, this.picture, this.currentPicture);
    this.getLandmarks(this.canvasLandmarks, this.printData);
  }

  initCanvasSize(): void {
    this.canvasPicture.nativeElement.setAttribute('height', String(this.picture.sizeCanvas.height));
    this.canvasPicture.nativeElement.setAttribute('width', String(this.picture.sizeCanvas.width));

    this.canvasLandmarks.nativeElement.setAttribute('height', String(this.picture.sizeCanvas.height));
    this.canvasLandmarks.nativeElement.setAttribute('width', String(this.picture.sizeCanvas.width));
  }

  loadPicture(canvasEl: ElementRef<HTMLCanvasElement>, picture, currentPicture): () => any {
    if (!!currentPicture) currentPicture();

    const { imageBitmap, sizeCanvas } = picture;
    const ctx: CanvasRenderingContext2D = canvasEl.nativeElement.getContext('2d');

    ctx.drawImage(imageBitmap, 0, 0, sizeCanvas.width, sizeCanvas.height);

    return () => ctx.clearRect(0, 0, sizeCanvas.width, sizeCanvas.height);
  }

  getLandmarks(canvasEl: ElementRef<HTMLCanvasElement>, data: RequestResultRecognition[] | FaceMatches[] | SourceImageFace[]): void {
    if (!data) return;

    const ctx: CanvasRenderingContext2D = canvasEl.nativeElement.getContext('2d');
    ctx.fillStyle = '#27C224';

    data.forEach(val => this.displayLandmarks(ctx, val));
  }

  displayLandmarks(ctx: CanvasRenderingContext2D, data: RequestResultRecognition | FaceMatches | SourceImageFace): void {
    // eslint-disable-next-line @typescript-eslint/naming-convention
    const { x_max, x_min, y_max, y_min } = data.box;
    const frameArea: number = (Math.round(x_max - x_min) * Math.round(y_max - y_min)) / 19000;
    const sizePoint = frameArea > 1.4 || data.landmarks.length >= 108 ? frameArea : 1.4;

    data.landmarks.forEach(landmark => {
      ctx.beginPath();
      ctx.strokeStyle = '#27C224';
      ctx.arc(landmark[0], landmark[1], sizePoint, 0, Math.PI * 2, false);
      ctx.fill();
      ctx.closePath();
      ctx.stroke();
    });
  }
}
