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
import { Component, Input, OnChanges, Output, EventEmitter, OnInit, SimpleChanges } from '@angular/core';

import { BehaviorSubject, defer, Observable, of } from 'rxjs';
import { filter, map, switchMap } from 'rxjs/operators';

import { FaceMatches, RequestResultVerification, SourceImageFace } from '../../../../data/interfaces/response-result';
import { RequestInfo } from '../../../../data/interfaces/request-info';
import { LoadingPhotoService } from '../../../../core/photo-loader/photo-loader.service';
import { ImageSize } from '../../../../data/interfaces/image';
import { recalculateFaceCoordinate, recalculateLandmarks, resultRecognitionFormatter } from '../../face-services.helpers';
import { VerificationServiceFields } from '../../../../data/enums/verification-service.enum';
import { ImageConvert } from '../../../../data/interfaces/image-convert';
import { ServiceTypes } from '../../../../data/enums/service-types.enum';

@Component({
  selector: 'app-verification-result',
  templateUrl: './verification-result.component.html',
  styleUrls: ['./verification-result.component.scss'],
})
export class VerificationResultComponent implements OnChanges, OnInit {
  @Input() processFile: File;
  @Input() checkFile: File;
  @Input() requestInfo: RequestInfo;
  @Input() printData: RequestResultVerification[];
  @Input() isLoaded: boolean;
  @Input() pending: boolean;
  @Input() type: ServiceTypes;

  @Output() selectProcessFile = new EventEmitter();
  @Output() selectCheckFile = new EventEmitter();
  @Output() resetProcessFile = new EventEmitter();
  @Output() resetCheckFile = new EventEmitter();

  private processFileConvert$: BehaviorSubject<File> = new BehaviorSubject(null);
  private checkFileConvert$: BehaviorSubject<File> = new BehaviorSubject(null);
  private dataPrintRecalculate$: BehaviorSubject<RequestResultVerification[]> = new BehaviorSubject(null);
  private verificationServiceFields = VerificationServiceFields;

  processPicture$: Observable<ImageConvert>;
  checkPicture$: Observable<ImageConvert>;
  processFileData$: Observable<SourceImageFace[]>;
  checkFileData$: Observable<FaceMatches[]>;
  widthCanvas = 500;
  formattedResult: string;

  constructor(private loadingPhotoService: LoadingPhotoService) {}

  ngOnInit(): void {
    this.processPicture$ = this.processFileConvert$.pipe(
      filter(file => !!file),
      switchMap(file => this.displayPhotoConvert(file))
    );

    this.processFileData$ = this.dataPrintRecalculate$.pipe(
      map(data => (data ? [data[0][this.verificationServiceFields.ProcessFileData]] : null)),
      switchMap(printData =>
        defer(() =>
          !!printData
            ? this.processPicture$.pipe(switchMap(sizes => this.printDataRecalculate(printData, sizes.imageBitmap, sizes.sizeCanvas)))
            : of(null)
        )
      )
    );

    this.checkPicture$ = this.checkFileConvert$.pipe(
      filter(file => !!file),
      switchMap(file => this.displayPhotoConvert(file))
    );

    this.checkFileData$ = this.dataPrintRecalculate$.pipe(
      map(data => (data ? data[0][this.verificationServiceFields.CheckFileData] : null)),
      switchMap(printData =>
        defer(() =>
          !!printData
            ? this.checkPicture$.pipe(switchMap(sizes => this.printDataRecalculate(printData, sizes.imageBitmap, sizes.sizeCanvas)))
            : of(null)
        )
      )
    );
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.hasOwnProperty('processFile')) this.processFileConvert$.next(this.processFile);
    if (changes.hasOwnProperty('checkFile')) this.checkFileConvert$.next(this.checkFile);
    if (changes.hasOwnProperty('printData')) this.dataPrintRecalculate$.next(this.printData);

    if (!!this.requestInfo) this.formattedResult = resultRecognitionFormatter(this.requestInfo.response);
  }

  printDataRecalculate<Type extends any[]>(data: Type, sizeImage: ImageSize, sizeCanvas: ImageSize): Observable<Type> {
    return new Observable(observer => {
      const recalculate = data.map(val => ({
        ...val,
        box: recalculateFaceCoordinate(val.box, sizeImage, sizeCanvas),
        landmarks: recalculateLandmarks(val.landmarks, sizeImage, sizeCanvas),
      })) as Type;
      observer.next(recalculate);
      observer.complete();
    });
  }

  displayPhotoConvert(file: File): Observable<ImageConvert> {
    return this.loadingPhotoService.loader(file).pipe(
      map(img => ({
        imageBitmap: img,
        sizeCanvas: { width: this.widthCanvas, height: (img.height / img.width) * this.widthCanvas },
      }))
    );
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
