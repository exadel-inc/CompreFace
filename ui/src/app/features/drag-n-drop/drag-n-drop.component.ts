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

import { Component, ViewChild, ElementRef, Input, Output } from '@angular/core';
import { Model } from 'src/app/data/model';
import { DragNDropService } from './drag-n-drop.service';
import { EventEmitter } from '@angular/core';

@Component({
  selector: 'app-drag-n-drop',
  templateUrl: './drag-n-drop.component.html',
  styleUrls: ['./drag-n-drop.component.scss']
})
export class DragNDropComponent {

  @ViewChild('fileDropRef', { static: false }) fileDropEl: ElementRef;
  file: any;
  @Input() data: object;
  @Input() loading = true;
  @Input() model: Model;
  @Output() recognizeFace = new EventEmitter();
  @ViewChild('myCanvas', { static: true }) myCanvas: ElementRef;

  constructor(private dragService: DragNDropService) {}

  /**
   * on file drop handler
   */
  onFileDropped($event) {
    this.processFileRecoFace($event);
  }

  /**
   * handle file from browsing
   */
  fileBrowseHandler(files) {
    this.processFileRecoFace(files);
  }

  /**
   * Recognize face
   * @param files (Files List)
   * TODO Send file to api
   */
  processFileRecoFace(files: Array<any>) {
    this.file = files[0];
    this.recognizeFace.emit(this.file);
  }

  printResult(box: any, face: any) {
    const img = new Image();
    const ctx: CanvasRenderingContext2D =
    this.myCanvas.nativeElement.getContext('2d');
    img.onload = () => {
      ctx.drawImage(img, 0, 0);
      ctx.beginPath();
      ctx.strokeStyle = 'green';
      ctx.moveTo(box.x_min, box.y_min);
      ctx.lineTo(box.x_max, box.y_min);
      ctx.lineTo(box.x_max, box.y_max);
      ctx.lineTo(box.x_min, box.y_max);
      ctx.lineTo(box.x_min, box.y_min);
      ctx.stroke();
      ctx.fillStyle = 'green';
      ctx.fillRect(box.x_min, box.y_min - 25, 200, 25);
      ctx.fillRect(box.x_min, box.y_max, 200, 25);
      ctx.fillStyle = 'white';
      ctx.font = '20pt Roboto Regular Helvetica Neue sans-serif';
      ctx.fillText(box.probability, box.x_min + 10, box.y_max + 20);
      ctx.fillText(face[0].face_name, box.x_min + 10, box.y_min - 5);
    };
    img.src = URL.createObjectURL(this.file);
  }
}
