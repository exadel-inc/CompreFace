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
import { Component, Input, OnChanges, SimpleChanges, Output, EventEmitter, OnInit, ChangeDetectionStrategy } from '@angular/core';

import { filter, map, switchMap, tap } from 'rxjs/operators';
import { BehaviorSubject, defer, Observable, of } from 'rxjs';

import { RequestResultRecognition } from '../../../../data/interfaces/response-result';
import { RequestInfo } from '../../../../data/interfaces/request-info';
import { LoadingPhotoService } from '../../../../core/photo-loader/photo-loader.service';
import { ServiceTypes } from '../../../../data/enums/service-types.enum';
import { ImageSize } from '../../../../data/interfaces/image';
import { recalculateFaceCoordinate, recalculateLandmarks, resultRecognitionFormatter } from '../../face-services.helpers';
import { ImageConvert } from '../../../../data/interfaces/image-convert';

@Component({
  selector: 'app-recognition-result',
  templateUrl: './recognition-result.component.html',
  styleUrls: ['./recognition-result.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RecognitionResultComponent implements OnInit, OnChanges {
  @Input() file: File;
  @Input() requestInfo: RequestInfo;
  @Input() isLoaded: boolean;
  @Input() type: ServiceTypes;
  @Input() printData: RequestResultRecognition[];

  @Output() selectFile = new EventEmitter();
  @Output() resetFace = new EventEmitter();

  dataPrintRecalculate$: BehaviorSubject<RequestResultRecognition[]> = new BehaviorSubject(null);
  convertFile$: BehaviorSubject<any> = new BehaviorSubject(null);

  picture$: Observable<ImageConvert>;
  printData$: Observable<RequestResultRecognition[]>;

  formattedResult: string;
  widthCanvas = 500;

  constructor(private loadingPhotoService: LoadingPhotoService) {}

  ngOnInit(): void {
    this.picture$ = this.convertFile$.pipe(
      filter(file => !!file),
      switchMap(file => this.displayPhotoConvert(file))
    );

    this.printData$ = this.dataPrintRecalculate$.pipe(
      switchMap(printData =>
        defer(() =>
          !!printData
            ? this.picture$.pipe(
                tap(data => console.log(data)),
                switchMap(sizes => this.printDataRecalculate(printData, sizes.imageBitmap, sizes.sizeCanvas))
              )
            : of(null)
        )
      )
    );
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.hasOwnProperty('file')) this.convertFile$.next(this.file);
    if (changes.hasOwnProperty('printData')) this.dataPrintRecalculate$.next(this.printData);

    if (!!this.requestInfo) this.formattedResult = resultRecognitionFormatter(this.requestInfo.response);
  }

  displayPhotoConvert(file: File): Observable<ImageConvert> {
    return this.loadingPhotoService.loader(file).pipe(
      map(img => ({
        imageBitmap: img,
        sizeCanvas: { width: this.widthCanvas, height: (img.height / img.width) * this.widthCanvas },
      }))
    );
  }

  printDataRecalculate<Type extends RequestResultRecognition[]>(data: Type, sizeImage: ImageSize, sizeCanvas: ImageSize): Observable<Type> {
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

  onResetFile(event?: File): void {
    this.resetFace.emit();
    if (event) this.selectFile.emit(event);
  }
}
