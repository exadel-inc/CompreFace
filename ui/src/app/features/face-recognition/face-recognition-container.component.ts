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
import { Observable } from 'rxjs';
import { Store } from '@ngrx/store';
import { AppState } from '../../store';
import { recognizeFace, recognizeFaceReset } from '../../store/face-recognition/actions';
import { selectFaceData, selectFile, selectRequest, selectTestIsPending, selectStateReady
} from '../../store/face-recognition/selectors';
import { MAX_IMAGE_SIZE } from 'src/app/core/constants';
import { SnackBarService } from '../snackbar/snackbar.service';

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
  isLoaded$: Observable<boolean>;

  @Input()
  title: string;

  constructor(
    private store: Store<AppState>,
    private snackBarService: SnackBarService
  ) { }

  ngOnInit() {
    this.data$ = this.store.select(selectFaceData);
    this.file$ = this.store.select(selectFile);
    this.requestInfo$ = this.store.select(selectRequest);
    this.pending$ = this.store.select(selectTestIsPending);
    this.isLoaded$ = this.store.select(selectStateReady);
  }

  ngOnDestroy() {
    this.store.dispatch(recognizeFaceReset());
  }

  recognizeFace(file: File) {
    file.size > MAX_IMAGE_SIZE ?
      this.snackBarService.openError('face_recognition_container.file_size_error')
    :
      this.store.dispatch(recognizeFace({file}));
  }
}
