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

import { Component, ElementRef, Input, OnDestroy, ViewChild } from '@angular/core';
import { Observable, Subscription } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { getImageSize, ImageSize, recalculateFaceCoordinate } from '../face-recognition.helpers';

@Component({
  selector: 'app-recognition-result',
  templateUrl: './recognition-result.component.html',
  styleUrls: ['./recognition-result.component.scss'],
})
export class RecognitionResultComponent implements OnDestroy {
  @Input() pending = true;
  @Input() file: File;
  @Input() requestInfo: any;
  // Handle input changes and update image.
  @Input() set printData(value: any) {
    if (this.printSubscription) {
      this.printSubscription.unsubscribe();
    }

    if (value) {
      this.printSubscription = this.printResult(value.box, value.faces).subscribe();
    }
  }
  @ViewChild('canvasElement', { static: true }) myCanvas: ElementRef;

  canvasSize: ImageSize = { width: 500, height: null };
  widthOfTextArea = 25;
  private printSubscription: Subscription;

  ngOnDestroy() {
    if (this.printSubscription) {
      this.printSubscription.unsubscribe();
    }
  }

  /*
   * Print result on template.
   *
   * @param box Box
   * @param face Face
   */
  printResult(box: any, face: any): Observable<any> {
    return getImageSize(this.file).pipe(
      tap(({ width, height }) => {
        this.canvasSize.height = (height / width) * this.canvasSize.width;
        this.myCanvas.nativeElement.setAttribute('height', this.canvasSize.height);
      }),
      map((imageSize) => recalculateFaceCoordinate(box, imageSize, this.canvasSize, this.widthOfTextArea)),
      tap((recalculatedBox) => this.drawCanvas(recalculatedBox, face))
    );
  }

  /*
   * Make canvas and draw face and info on image.
   *
   * @param box Face coordinates from BE.
   * @param face.
   */
  drawCanvas(box: any, face: any) {
    const img = new Image();
    const resultFace = face.length > 0 ? face[0] : { face_name: undefined, similarity: 0 };
    const ctx: CanvasRenderingContext2D = this.myCanvas.nativeElement.getContext('2d');

    img.onload = () => {
      ctx.drawImage(img, 0, 0, this.canvasSize.width, this.canvasSize.height);
      ctx.beginPath();
      ctx.strokeStyle = 'green';
      ctx.moveTo(box.x_min, box.y_min);
      ctx.lineTo(box.x_max, box.y_min);
      ctx.lineTo(box.x_max, box.y_max);
      ctx.lineTo(box.x_min, box.y_max);
      ctx.lineTo(box.x_min, box.y_min);
      ctx.stroke();
      ctx.fillStyle = 'green';
      ctx.fillRect(box.x_min, box.y_min - this.widthOfTextArea, box.x_max - box.x_min, this.widthOfTextArea);
      ctx.fillRect(box.x_min, box.y_max, box.x_max - box.x_min, this.widthOfTextArea);
      ctx.fillStyle = 'white';
      ctx.font = '12pt Roboto Regular Helvetica Neue sans-serif';
      ctx.fillText(resultFace.similarity, box.x_min + 10, box.y_max + 20);
      ctx.fillText(resultFace.face_name, box.x_min + 10, box.y_min - 5);
    };
    img.src = URL.createObjectURL(this.file);
  }
}
