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
import { ChangeDetectionStrategy, Component, Input, OnDestroy, OnInit } from '@angular/core';
import { Store } from '@ngrx/store';

import { switchMap } from 'rxjs/operators';
import { defer, Observable, of, Subscription } from 'rxjs';

import { AVAILABLE_IMAGE_EXTENSIONS } from 'src/app/core/constants';

import { AppState } from '../../../store';
import { recognizeFace, recognizeFaceReset } from '../../../store/face-recognition/action';
import {
  selectFaceData,
  selectFile,
  selectRequest,
  selectStateReady,
  selectTestIsPending,
} from '../../../store/face-recognition/selectors';
import { getFileExtension } from '../face-services.helpers';
import { SnackBarService } from '../../snackbar/snackbar.service';
import { ServiceTypes } from '../../../data/enums/service-types.enum';
import { LoadingPhotoService } from '../../../core/photo-loader/photo-loader.service';
import { selectMaxFileSize } from 'src/app/store/image-size/selectors';

@Component({
  selector: 'app-face-recognition-container',
  templateUrl: './face-recognition-container.component.html',
  styleUrls: ['./face-recognition-container.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FaceRecognitionContainerComponent implements OnInit, OnDestroy {
  data$: Observable<any>;
  photo$: Observable<any>;
  requestInfo$: Observable<any>;
  pending$: Observable<boolean>;
  isLoaded$: Observable<boolean>;
  maxImageSize: number;
  imageSizeSubs: Subscription;

  @Input() title: string;
  @Input() type: ServiceTypes;

  constructor(private store: Store<AppState>, private snackBarService: SnackBarService, private loadingPhotoService: LoadingPhotoService) {}

  ngOnInit() {
    this.data$ = this.store.select(selectFaceData);
    this.requestInfo$ = this.store.select(selectRequest);
    this.pending$ = this.store.select(selectTestIsPending);
    this.isLoaded$ = this.store.select(selectStateReady);
    this.photo$ = this.store
      .select(selectFile)
      .pipe(switchMap(file => defer(() => (!!file ? this.loadingPhotoService.loader(file) : of(null)))));
    this.imageSizeSubs = this.store.select(selectMaxFileSize).subscribe(res => (this.maxImageSize = res.clientMaxFileSize));
  }

  resetFace(): void {
    this.store.dispatch(recognizeFaceReset());
  }

  recognizeFace(file: File) {
    if (!AVAILABLE_IMAGE_EXTENSIONS.includes(getFileExtension(file))) {
      this.snackBarService.openNotification({
        messageText: 'face_recognition_container.file_unavailable_extension',
        messageOptions: { filename: file.name },
        type: 'error',
      });
    } else if (file.size > this.maxImageSize) {
      this.snackBarService.openNotification({ messageText: 'face_recognition_container.file_size_error', type: 'error' });
    } else {
      this.store.dispatch(recognizeFace({ file }));
    }
  }

  ngOnDestroy() {
    this.resetFace();
    this.imageSizeSubs.unsubscribe();
  }
}
