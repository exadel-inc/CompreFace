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
import { AfterViewInit, Component, ElementRef, Input, ViewChild } from '@angular/core';

import { FaceMatches, RequestResultRecognition, SourceImageFace } from '../../../data/interfaces/response-result';

@Component({
  selector: 'app-face-landmarks',
  template: '<canvas #canvasElement [width]="parentCanvas.width" [height]="parentCanvas.height"></canvas>',
  styles: ['canvas { position: absolute; top: 0; left: 0 }'],
})
export class FaceLandmarksComponent implements AfterViewInit {
  @Input() parentCanvas: HTMLCanvasElement;
  @Input() printData: RequestResultRecognition[] | FaceMatches[] | SourceImageFace[];

  @ViewChild('canvasElement', { static: false }) canvas: ElementRef<HTMLCanvasElement>;

  private ctx: CanvasRenderingContext2D;
  private colorLandmarks = '#27C224';

  ngAfterViewInit(): void {
    this.ctx = this.canvas.nativeElement.getContext('2d');
    this.ctx.fillStyle = this.colorLandmarks;
    this.getLandmarks(this.printData);
  }

  getLandmarks(data: RequestResultRecognition[] | FaceMatches[] | SourceImageFace[]): void {
    if (!data) return;

    data.forEach(val => this.displayLandmarks(val));
  }

  displayLandmarks(data: RequestResultRecognition | FaceMatches | SourceImageFace): void {
    // eslint-disable-next-line @typescript-eslint/naming-convention
    const { x_max, x_min, y_max, y_min } = data.box;
    const frameArea: number = Math.round(x_max - x_min) * Math.round(y_max - y_min);
    const px = frameArea / 3000 / 10 > 1.4 || data.landmarks.length >= 108 ? frameArea / 3000 / 10 : 1.4;

    data.landmarks.forEach(val => this.drawLandmarks(val, this.ctx, this.colorLandmarks, px));
  }

  drawLandmarks(box: number[], ctx: CanvasRenderingContext2D, color: string, sizePoint: number) {
    ctx.beginPath();
    ctx.strokeStyle = color;
    ctx.arc(box[0], box[1], sizePoint, 0, Math.PI * 2, false);
    ctx.fill();
    ctx.closePath();
    ctx.stroke();
  }
}
