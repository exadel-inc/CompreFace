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

import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { combineLatest, Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Store } from '@ngrx/store';
import { AppState } from '../../store';
import { recognizeFace, recognizeFaceReset } from '../../store/face-recognition/actions';
import { selectFaceData, selectFile, selectRequest,
  selectTestIsPending } from '../../store/face-recognition/selectors';

@Component({
  selector: 'app-face-recognition-container',
  templateUrl: './face-recognition-container.component.html',
  styleUrls: ['./face-recognition-container.component.scss']
})
export class FaceRecognitionContainerComponent implements OnInit, OnDestroy {
  data$: Observable<any>;
  file$: Observable<any>;
  requestInfo$: Observable<any>;
  pending$: Observable<boolean>;
  isDisplayResult$: Observable<boolean>;

  @Input()
  apiKey: string;
  @Input()
  title: string;

  constructor(private store: Store<AppState>) {
    // Component constructor.
  }

  ngOnInit() {
    this.data$ = this.store.select(selectFaceData);
    this.file$ = this.store.select(selectFile);
    this.requestInfo$ = this.store.select(selectRequest);
    this.pending$ = this.store.select(selectTestIsPending);
    this.isDisplayResult$ = combineLatest([this.data$, this.pending$]).pipe(
      map(([data, pending]) => !!data && !pending)
    );
  }

  ngOnDestroy() {
    this.store.dispatch(recognizeFaceReset());
  }

  recognizeFace(file: File) {
    return this.store.dispatch(recognizeFace({
      file,
      apiKey: this.apiKey
    }));
  }
}
