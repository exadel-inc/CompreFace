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
  ElementRef,
  Input,
  ViewChild,
  OnChanges,
  SimpleChanges,
  Output,
  EventEmitter,
  AfterViewInit,
  OnInit,
  OnDestroy,
} from '@angular/core';

import { filter, map, switchMap, takeUntil, tap } from 'rxjs/operators';
import { BehaviorSubject, Observable, Subject } from 'rxjs';

import { RequestResultRecognition } from '../../../../data/interfaces/response-result';
import { RequestInfo } from '../../../../data/interfaces/request-info';
import { LoadingPhotoService } from '../../../../core/photo-loader/photo-loader.service';
import { ServiceTypes } from '../../../../data/enums/service-types.enum';
import { ImageSize } from '../../../../data/interfaces/image';
import { recalculateFaceCoordinate, resultRecognitionFormatter } from '../../face-services.helpers';

@Component({
  selector: 'app-recognition-result',
  templateUrl: './recognition-result.component.html',
  styleUrls: ['./recognition-result.component.scss'],
})
export class RecognitionResultComponent implements OnInit, OnChanges, OnDestroy, AfterViewInit {
  @Input() file: File;
  @Input() requestInfo: RequestInfo;
  @Input() isLoaded: boolean;
  @Input() type: string;
  @Input() printData: RequestResultRecognition[];

  @Output() selectFile = new EventEmitter();
  @Output() resetFace = new EventEmitter();

  @ViewChild('canvasElement', { static: true }) canvas: ElementRef<HTMLCanvasElement>;

  private ctx: CanvasRenderingContext2D;
  private dataPrint$: BehaviorSubject<RequestResultRecognition[]> = new BehaviorSubject(null);
  private dataPhoto$: BehaviorSubject<any> = new BehaviorSubject(null);
  private unsubscribe$: Subject<void> = new Subject();

  formattedResult: string;
  widthCanvas = 500;
  types = ServiceTypes;
  recalculatePrint: RequestResultRecognition[];

  constructor(private loadingPhotoService: LoadingPhotoService) {}

  ngOnInit(): void {
    this.dataPhoto$
      .pipe(
        takeUntil(this.unsubscribe$),
        filter(data => !!data),
        switchMap(data => this.displayPhoto(data)),
        switchMap(sizes => this.dataPrint$.pipe(switchMap(printData => this.displayFrames(printData, sizes.imageBitmap, sizes.sizeCanvas))))
      )
      .subscribe(value => (this.recalculatePrint = value));
  }

  ngAfterViewInit(): void {
    this.ctx = this.canvas.nativeElement.getContext('2d');
  }

  ngOnChanges(changes: SimpleChanges) {
    this.dataPhoto$.next(this.file);
    this.dataPrint$.next(this.printData);
    if (!!this.requestInfo) this.formattedResult = resultRecognitionFormatter(this.requestInfo.response);
  }

  displayPhoto(file: File): Observable<any> {
    return this.loadingPhotoService.loader(file).pipe(
      map(img => ({
        imageBitmap: img,
        sizeCanvas: { width: this.widthCanvas, height: (img.height / img.width) * this.widthCanvas },
      })),
      tap(({ sizeCanvas }) => this.canvas.nativeElement.setAttribute('height', String(sizeCanvas.height))),
      tap(({ imageBitmap, sizeCanvas }) => this.ctx.drawImage(imageBitmap, 0, 0, sizeCanvas.width, sizeCanvas.height))
    );
  }

  displayFrames<Type extends RequestResultRecognition[]>(data: Type, sizeImage: ImageSize, sizeCanvas: ImageSize): Observable<Type> {
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

  onResetFile(event?: File): void {
    const { offsetHeight, offsetWidth } = this.ctx.canvas;
    this.ctx.clearRect(0, 0, offsetWidth, offsetHeight);

    this.resetFace.emit();

    if (event) this.selectFile.emit(event);
  }

  ngOnDestroy(): void {
    this.unsubscribe$.next();
    this.unsubscribe$.complete();
  }
}
