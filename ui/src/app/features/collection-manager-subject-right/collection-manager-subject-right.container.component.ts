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
import { ChangeDetectionStrategy, Component, EventEmitter, OnDestroy, OnInit, Output } from '@angular/core';
import { Observable, Subscription } from 'rxjs';

import { CollectionRightFacade } from './collection-manager-right-facade';
import { CircleLoadingProgressEnum } from 'src/app/data/enums/circle-loading-progress.enum';
import { SubjectModeEnum } from 'src/app/data/enums/subject-mode.enum';
import { CollectionItem } from 'src/app/data/interfaces/collection';
import { take, tap } from 'rxjs/operators';
import { SnackBarService } from '../snackbar/snackbar.service';
import { selectMaxFileSize } from 'src/app/store/image-size/selectors';
import { Store } from '@ngrx/store';
import { AppState } from 'src/app/store';

@Component({
  selector: 'app-application-right-container',
  template: `<app-collection-manager-subject-right
    [subject]="subject$ | async"
    [apiKey]="apiKey$ | async"
    [collectionItems]="collectionItems$ | async"
    [isPending]="isPending$ | async"
    [isCollectionPending]="isCollectionPending$ | async"
    [mode]="mode$ | async"
    [defaultSubject]="defaultSubject$ | async"
    [maxImageSize]="maxImageSize"
    (initApiKey)="initApiKey($event)"
    (readFiles)="readFiles($event)"
    (deleteItem)="deleteItem($event)"
    (cancelUploadItem)="cancelUploadItem($event)"
    (loadMore)="loadMore($event)"
    (selectExample)="selectExample($event)"
    (restartUploading)="restartUploading()"
  ></app-collection-manager-subject-right>`,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CollectionManagerSubjectRightContainerComponent implements OnInit, OnDestroy {
  defaultSubject$: Observable<string>;
  subject$: Observable<string>;
  isPending$: Observable<boolean>;
  isCollectionPending$: Observable<boolean>;
  apiKey$: Observable<string>;
  maxImageSize: number;
  collectionItems$: Observable<CollectionItem[]>;
  mode$: Observable<SubjectModeEnum>;
  apiKeyInitSubscription: Subscription;

  @Output() setDefaultMode = new EventEmitter();
  private apiKey: string;

  constructor(
    private collectionRightFacade: CollectionRightFacade,
    private snackBarService: SnackBarService,
    private store: Store<AppState>
  ) {}

  ngOnInit(): void {
    this.subject$ = this.collectionRightFacade.subject$;
    this.apiKey$ = this.collectionRightFacade.apiKey$;
    this.collectionItems$ = this.collectionRightFacade.collectionItems$;
    this.isPending$ = this.collectionRightFacade.isPending$;
    this.isCollectionPending$ = this.collectionRightFacade.isCollectionPending$;
    this.mode$ = this.collectionRightFacade.subjectMode$;
    this.store
      .select(selectMaxFileSize)
      .pipe(
        take(1),
        tap(res => (this.maxImageSize = res.clientMaxFileSize))
      )
      .subscribe();

    this.defaultSubject$ = this.collectionRightFacade.defaultSubject$.pipe(
      tap(subject => this.collectionRightFacade.loadSubjectMedia(subject))
    );
  }

  initApiKey(apiKey: string): void {
    this.apiKey = apiKey;
  }

  loadMore(item: CollectionItem): void {
    this.collectionRightFacade.loadNextPage(item['subject'], item['page'], item['totalPages']);
  }

  restartUploading(): void {
    this.collectionRightFacade.restartUploading();
  }

  readFiles(fileList: File[]): void {
    this.setDefaultMode.emit();
    const fileBodySize = fileList.map(item => item.size).reduce((previousValue, currentValue) => previousValue + currentValue);

    this.collectionRightFacade.addImageFilesToCollection(fileList);
  }

  deleteItem(item: CollectionItem): void {
    if (item.status === CircleLoadingProgressEnum.Uploaded) {
      this.collectionRightFacade.deleteSubjectExample(item);

      return;
    }

    this.collectionRightFacade.deleteItemFromUploadOrder(item);
  }

  cancelUploadItem(item: CollectionItem): void {
    this.collectionRightFacade.deleteItemFromUploadOrder(item);
  }

  selectExample(item: CollectionItem): void {
    this.collectionRightFacade.selectSubjectExample(item);
  }

  ngOnDestroy(): void {
    this.collectionRightFacade.resetSubjectExamples();
  }
}
