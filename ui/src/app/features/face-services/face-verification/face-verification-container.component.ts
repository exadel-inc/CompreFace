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

import { defer, Observable, of, Subscription } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';

import { AVAILABLE_IMAGE_EXTENSIONS } from 'src/app/core/constants';

import { AppState } from '../../../store';
import {
  verifyFaceReset,
  verifyFaceProcessFileReset,
  verifyFaceCheckFileReset,
  verifyFaceAddProcessFile,
  verifyFaceAddCheckFileFile,
} from '../../../store/face-verification/action';
import {
  selectCheckFile,
  selectFaceData,
  selectProcessFile,
  selectRequest,
  selectStateReady,
  selectTestIsPending,
} from '../../../store/face-verification/selectors';

import { getFileExtension } from '../face-services.helpers';
import { SnackBarService } from '../../snackbar/snackbar.service';
import { ServiceTypes } from '../../../data/enums/service-types.enum';
import { LoadingPhotoService } from '../../../core/photo-loader/photo-loader.service';
import { VerificationServiceFields } from '../../../data/enums/verification-service.enum';
import { selectMaxFileSize } from 'src/app/store/image-size/selectors';

@Component({
  selector: 'app-face-verification-container',
  templateUrl: './face-verification-container.component.html',
  styleUrls: ['./face-verification-container.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FaceVerificationContainerComponent implements OnInit, OnDestroy {
  processPhoto$: Observable<ImageBitmap>;
  dataProcessPhoto$: Observable<any>;
  checkPhoto$: Observable<ImageBitmap>;
  dataCheckPhoto$: Observable<any>;
  requestInfo$: Observable<any>;
  pending$: Observable<boolean>;
  isLoaded$: Observable<boolean>;
  imageSizeSubs: Subscription;
  maxImageSize: number;

  @Input() type: ServiceTypes;

  fields = VerificationServiceFields;

  constructor(private store: Store<AppState>, private snackBarService: SnackBarService, private loadingPhotoService: LoadingPhotoService) {}

  ngOnInit() {
    this.dataProcessPhoto$ = this.store.select(selectFaceData).pipe(map(data => (data ? [data?.[0][this.fields.ProcessFileData]] : null)));
    this.dataCheckPhoto$ = this.store.select(selectFaceData).pipe(map(data => (data ? data?.[0][this.fields.CheckFileData] : null)));
    this.requestInfo$ = this.store.select(selectRequest);
    this.pending$ = this.store.select(selectTestIsPending);
    this.isLoaded$ = this.store.select(selectStateReady);
    this.processPhoto$ = this.store
      .select(selectProcessFile)
      .pipe(switchMap(file => defer(() => (!!file ? this.loadingPhotoService.loader(file) : of(null)))));
    this.checkPhoto$ = this.store
      .select(selectCheckFile)
      .pipe(switchMap(file => defer(() => (!!file ? this.loadingPhotoService.loader(file) : of(null)))));
    this.imageSizeSubs = this.store.select(selectMaxFileSize).subscribe(res => (this.maxImageSize = res.clientMaxFileSize));
  }

  ngOnDestroy() {
    this.store.dispatch(verifyFaceReset());
    this.imageSizeSubs.unsubscribe();
  }

  processFileUpload(file) {
    if (this.validateImage(file)) {
      this.store.dispatch(verifyFaceAddProcessFile({ processFile: file }));
    }
  }

  checkFileUpload(file) {
    if (this.validateImage(file)) {
      this.store.dispatch(verifyFaceAddCheckFileFile({ checkFile: file }));
    }
  }

  processFileReset(event?: File) {
    this.store.dispatch(verifyFaceProcessFileReset());
    if (!!event) {
      this.processFileUpload(event);
    }
  }

  checkFileReset(event: File) {
    this.store.dispatch(verifyFaceCheckFileReset());
    if (!!event) {
      this.checkFileUpload(event);
    }
  }

  validateImage(file) {
    if (!AVAILABLE_IMAGE_EXTENSIONS.includes(getFileExtension(file))) {
      this.snackBarService.openNotification({
        messageText: 'face_recognition_container.file_unavailable_extension',
        messageOptions: { filename: file.name },
        type: 'error',
      });
      return false;
    } else if (file.size > this.maxImageSize) {
      this.snackBarService.openNotification({ messageText: 'face_recognition_container.file_size_error', type: 'error' });
      return false;
    } else {
      return true;
    }
  }
}
