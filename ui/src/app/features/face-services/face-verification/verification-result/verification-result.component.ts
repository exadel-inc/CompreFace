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
import { Component, Input, OnChanges, SimpleChanges, Output, EventEmitter, ViewChild, ElementRef, OnDestroy } from '@angular/core';

import { ReplaySubject, Subject } from 'rxjs';
import { map, takeUntil, tap } from 'rxjs/operators';

import { FaceMatches, RequestResultVerification, SourceImageFace } from '../../../../data/interfaces/response-result';
import { RequestInfo } from '../../../../data/interfaces/request-info';
import { LoadingPhotoService } from '../../../../core/photo-loader/photo-loader.service';
import { ImageSize } from '../../../../data/interfaces/image';
import { recalculateFaceCoordinate, resultRecognitionFormatter } from '../../face-services.helpers';

enum PrintDataKeys {
  SourceFace = 'source_image_face',
  MatchesFace = 'face_matches',
}

@Component({
  selector: 'app-verification-result',
  templateUrl: './verification-result.component.html',
  styleUrls: ['./verification-result.component.scss'],
})
export class VerificationResultComponent implements OnChanges, OnDestroy {
  @Input() processFile: File;
  @Input() checkFile: File;

  @Input() requestInfo: RequestInfo;
  @Input() printData: RequestResultVerification[];

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

  private processFileCanvasLink: ElementRef;
  private checkFileCanvasLink: ElementRef;
  private unsubscribe: Subject<void> = new Subject();
  private sizes: ReplaySubject<{ key: PrintDataKeys; img: ImageBitmap; sizeCanvas: ImageSize }> = new ReplaySubject();

  widthCanvas = 500;
  processFilePrintData: FaceMatches[];
  checkFilePrintData: SourceImageFace[];
  formattedResult: string;

  constructor(private loadingPhotoService: LoadingPhotoService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if ('processFile' in changes) this.loadPhoto(this.processFile, this.processFileCanvasLink, PrintDataKeys.SourceFace);
    if ('checkFile' in changes) this.loadPhoto(this.checkFile, this.checkFileCanvasLink, PrintDataKeys.MatchesFace);
    if ('printData' in changes) this.getFrames(this.printData);
    if (!!this.requestInfo) this.formattedResult = resultRecognitionFormatter(this.requestInfo.response);
  }

  getFrames(printData: RequestResultVerification[]): void {
    if (!printData) return;

    this.sizes.pipe(takeUntil(this.unsubscribe)).subscribe(size => {
      switch (size.key) {
        case PrintDataKeys.MatchesFace:
          this.processFilePrintData = this.recalculateFrames(printData[0][size.key], size.img, size.sizeCanvas) as FaceMatches[];
          return;
        case PrintDataKeys.SourceFace:
          this.checkFilePrintData = this.recalculateFrames([printData[0][size.key]], size.img, size.sizeCanvas) as SourceImageFace[];
          return;
      }
    });
  }

  recalculateFrames(data: any[], img, sizeCanvas): SourceImageFace[] | FaceMatches[] {
    return data.map(val => ({ ...val, box: recalculateFaceCoordinate(val.box, img, sizeCanvas) }));
  }

  loadPhoto(file: File, canvas: ElementRef, key: PrintDataKeys): void {
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
        this.sizes.next({ key, ...value });
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
