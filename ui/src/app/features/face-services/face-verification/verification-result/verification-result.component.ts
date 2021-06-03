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
import {
  Component,
  Input,
  OnChanges,
  SimpleChanges,
  Output,
  EventEmitter,
  ViewChild,
  ElementRef,
  AfterViewInit,
  OnInit,
  OnDestroy,
} from '@angular/core';

import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { filter, map, switchMap, takeUntil, tap } from 'rxjs/operators';

import { FaceMatches, RequestResultVerification, SourceImageFace } from '../../../../data/interfaces/response-result';
import { RequestInfo } from '../../../../data/interfaces/request-info';
import { LoadingPhotoService } from '../../../../core/photo-loader/photo-loader.service';
import { ImageSize } from '../../../../data/interfaces/image';
import { recalculateFaceCoordinate, resultRecognitionFormatter } from '../../face-services.helpers';
import { VerificationServiceFields } from '../../../../data/enums/verification-service.enum';

@Component({
  selector: 'app-verification-result',
  templateUrl: './verification-result.component.html',
  styleUrls: ['./verification-result.component.scss'],
})
export class VerificationResultComponent implements OnInit, OnChanges, AfterViewInit, OnDestroy {
  @Input() processFile: File;
  @Input() checkFile: File;
  @Input() requestInfo: RequestInfo;
  @Input() printData: RequestResultVerification[];
  @Input() isLoaded: boolean;
  @Input() pending: boolean;

  @Output() selectProcessFile = new EventEmitter();
  @Output() selectCheckFile = new EventEmitter();
  @Output() resetProcessFile = new EventEmitter();
  @Output() resetCheckFile = new EventEmitter();

  @ViewChild('processFileCanvasElement', { static: true }) canvasProcessFile: ElementRef<HTMLCanvasElement>;
  @ViewChild('checkFileCanvasElement', { static: true }) canvasCheckFile: ElementRef<HTMLCanvasElement>;

  private ctxPhotoProcess: CanvasRenderingContext2D;
  private photoProcess$: BehaviorSubject<File> = new BehaviorSubject(null);

  private ctxPhotoCheck: CanvasRenderingContext2D;
  private photoCheck$: BehaviorSubject<File> = new BehaviorSubject(null);

  private unsubscribe$: Subject<void> = new Subject();
  private printData$: BehaviorSubject<RequestResultVerification[]> = new BehaviorSubject(null);

  widthCanvas = 500;
  formattedResult: string;
  recalculateProcessFile: SourceImageFace[];
  recalculateCheckFile: FaceMatches[];

  constructor(private loadingPhotoService: LoadingPhotoService) {}

  ngAfterViewInit(): void {
    this.ctxPhotoProcess = this.canvasProcessFile.nativeElement.getContext('2d');
    this.ctxPhotoCheck = this.canvasCheckFile.nativeElement.getContext('2d');
  }

  ngOnInit(): void {
    this.photoProcess$
      .pipe(
        takeUntil(this.unsubscribe$),
        filter(data => !!data),
        switchMap(data => this.displayPhoto(data, this.canvasProcessFile, this.ctxPhotoProcess)),
        switchMap(sizes => this.displayFrames(VerificationServiceFields.ProcessFileData, sizes)),
        map(data => data as SourceImageFace[])
      )
      .subscribe(value => (this.recalculateProcessFile = value));

    this.photoCheck$
      .pipe(
        takeUntil(this.unsubscribe$),
        filter(data => !!data),
        switchMap(data => this.displayPhoto(data, this.canvasCheckFile, this.ctxPhotoCheck)),
        switchMap(sizes => this.displayFrames(VerificationServiceFields.CheckFileData, sizes)),
        map(data => data as FaceMatches[])
      )
      .subscribe(value => (this.recalculateCheckFile = value));
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.photoProcess$.next(this.processFile);
    this.photoCheck$.next(this.checkFile);
    this.printData$.next(this.printData);

    if (!!this.requestInfo) this.formattedResult = resultRecognitionFormatter(this.requestInfo.response);
  }

  recalculateFrames<Type extends any[]>(data: Type, sizeImage: ImageSize, sizeCanvas: ImageSize): Observable<Type> {
    return new Observable(observer => {
      if (!!data) {
        const recalculate = data.map(val => ({ ...val, box: recalculateFaceCoordinate(val.box, sizeImage, sizeCanvas) })) as Type;
        observer.next(recalculate);
      } else {
        observer.next(null);
      }
      observer.complete();
    });
  }

  getDataFrames(type: VerificationServiceFields, result: RequestResultVerification[]): SourceImageFace[] | FaceMatches[] {
    switch (type) {
      case VerificationServiceFields.CheckFileData:
        return result[0][VerificationServiceFields.CheckFileData] as SourceImageFace[];
      case VerificationServiceFields.ProcessFileData:
        return [result[0][VerificationServiceFields.ProcessFileData]] as FaceMatches[];
    }
  }

  displayFrames(type: VerificationServiceFields, sizes): Observable<SourceImageFace[] | FaceMatches[]> {
    return this.printData$.pipe(
      map(data => (!!data ? this.getDataFrames(type, data) : null)),
      switchMap(printData => this.recalculateFrames(printData, sizes.imageBitmap, sizes.sizeCanvas))
    );
  }

  displayPhoto(file: File, el: ElementRef, ctx: CanvasRenderingContext2D): Observable<any> {
    return this.loadingPhotoService.loader(file).pipe(
      map(img => ({
        imageBitmap: img,
        sizeCanvas: { width: this.widthCanvas, height: (img.height / img.width) * this.widthCanvas },
      })),
      tap(({ sizeCanvas }) => el.nativeElement.setAttribute('height', String(sizeCanvas.height))),
      tap(({ imageBitmap, sizeCanvas }) => ctx.drawImage(imageBitmap, 0, 0, sizeCanvas.width, sizeCanvas.height))
    );
  }

  onResetProcessFile(event?: File): void {
    const { offsetHeight, offsetWidth } = this.ctxPhotoProcess.canvas;
    this.ctxPhotoProcess.clearRect(0, 0, offsetWidth, offsetHeight);

    this.resetProcessFile.emit();

    if (event) this.resetProcessFile.emit(event);
  }

  onResetCheckFile(event?: File): void {
    const { offsetHeight, offsetWidth } = this.ctxPhotoCheck.canvas;
    this.ctxPhotoCheck.clearRect(0, 0, offsetWidth, offsetHeight);

    this.resetCheckFile.emit();

    if (event) this.resetCheckFile.emit(event);
  }

  ngOnDestroy(): void {
    this.unsubscribe$.next();
    this.unsubscribe$.complete();
  }
}
