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
import { Component, ElementRef, Input, ViewChild, OnChanges, SimpleChanges, Output, EventEmitter, OnDestroy } from '@angular/core';

import { map, takeUntil, tap } from 'rxjs/operators';
import { ReplaySubject, Subject } from 'rxjs';

import { RequestResultRecognition } from '../../../../data/interfaces/response-result';
import { RequestInfo } from '../../../../data/interfaces/request-info';
import { LoadingPhotoService } from '../../../../core/photo-loader/photo-loader.service';
import { ImageSize } from '../../../../data/interfaces/image';
import { recalculateFaceCoordinate, resultRecognitionFormatter } from '../../face-services.helpers';

@Component({
  selector: 'app-recognition-result',
  templateUrl: './recognition-result.component.html',
  styleUrls: ['./recognition-result.component.scss'],
})
export class RecognitionResultComponent implements OnChanges, OnDestroy {
  @Input() file: File;
  @Input() requestInfo: RequestInfo;
  @Input() printData: RequestResultRecognition;
  @Input() isLoaded: boolean;
  @Input() type: string;

  @Output() selectFile = new EventEmitter();

  @ViewChild('canvasElement') set canvasElement(canvas: ElementRef) {
    this.myCanvas = canvas;
  }

  private myCanvas: ElementRef;
  private unsubscribe: Subject<void> = new Subject();
  private sizes: ReplaySubject<any> = new ReplaySubject();

  filePrintData: any;
  widthCanvas = 500;
  formattedResult: string;

  constructor(private loadingPhotoService: LoadingPhotoService) {}

  ngOnChanges(changes: SimpleChanges) {
    if ('file' in changes) this.loadPhoto(this.file, this.myCanvas);
    if ('printData' in changes) this.getFrames(this.printData);
    if (!!this.requestInfo) this.formattedResult = resultRecognitionFormatter(this.requestInfo.response);
  }

  getFrames(printData: any): void {
    if (!printData) return;

    this.sizes.pipe(takeUntil(this.unsubscribe)).subscribe(size => {
      this.filePrintData = this.recalculateFrames(printData, size.img, size.sizeCanvas);
    });
  }

  recalculateFrames(data: any[], img, sizeCanvas): any {
    return data.map(val => ({ ...val, box: recalculateFaceCoordinate(val.box, img, sizeCanvas) }));
  }

  loadPhoto(file: File, canvas: ElementRef): void {
    if (!file) return;

    this.loadingPhotoService
      .loader(file)
      .pipe(
        takeUntil(this.unsubscribe),
        map((img: ImageBitmap) => ({
          img,
          sizeCanvas: { width: this.widthCanvas, height: (img.height / img.width) * this.widthCanvas },
        })),
        tap(({ sizeCanvas }) => canvas.nativeElement.setAttribute('height', sizeCanvas.height))
      )
      .subscribe(value => {
        this.displayPhoto(value.img, value.sizeCanvas, canvas);
        this.sizes.next(value);
      });
  }

  displayPhoto(img: ImageBitmap, size: ImageSize, canvas: ElementRef): void {
    const ctx = canvas.nativeElement.getContext('2d');
    ctx.drawImage(img, 0, 0, size.width, size.height);
  }

  ngOnDestroy(): void {
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }
}
