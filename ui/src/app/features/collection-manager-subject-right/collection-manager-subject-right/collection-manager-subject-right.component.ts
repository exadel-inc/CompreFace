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
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { CircleLoadingProgressEnum } from 'src/app/data/enums/circle-loading-progress.enum';
import { SubjectModeEnum } from 'src/app/data/enums/subject-mode.enum';
import { CollectionItem } from 'src/app/data/interfaces/collection';

@Component({
  selector: 'app-collection-manager-subject-right',
  templateUrl: './collection-manager-subject-right.component.html',
  styleUrls: ['./collection-manager-subject-right.component.scss'],
})
export class CollectionManagerSubjectRightComponent implements OnChanges {
  scrollWindow: boolean = false;
  prevItemCollection: CollectionItem[] = [];
  subjectModeEnum = SubjectModeEnum;
  uploadedExamples: CollectionItem[] = [];
  isCollectionOnHold: boolean = false;
  totalElements: number = 0;

  @Input() isPending: boolean;
  @Input() isCollectionPending: boolean;
  @Input() subject: string;
  @Input() subjects: string[];
  @Input() set apiKey(value: string) {
    if (!!value) this.initApiKey.emit(value);
  }
  @Input() collectionItems: CollectionItem[];
  @Input() mode = SubjectModeEnum.Default;
  @Input() selectedIds: string[];

  @Output() deleteSubject = new EventEmitter<string>();
  @Output() editSubject = new EventEmitter<string>();
  @Output() initApiKey = new EventEmitter<string>();
  @Output() readFiles = new EventEmitter<File[]>();
  @Output() deleteItem = new EventEmitter<CollectionItem>();
  @Output() cancelUploadItem = new EventEmitter<CollectionItem>();
  @Output() setMode = new EventEmitter<SubjectModeEnum>();
  @Output() deleteSelectedExamples = new EventEmitter<string[]>();
  @Output() selectExample = new EventEmitter<CollectionItem>();
  @Output() loadMore = new EventEmitter<CollectionItem>();
  @Output() restartUploading = new EventEmitter();

  ngOnChanges(changes: SimpleChanges) {
    const change = changes['collectionItems'];

    if (change && change['currentValue'] !== change['previousValue']) {
      const collectionOnHold = this.collectionItems.filter(item => item.status === CircleLoadingProgressEnum.OnHold);

      this.isCollectionOnHold = !!collectionOnHold.length;

      const itemInProgress = this.collectionItems.find(item => item.status === CircleLoadingProgressEnum.InProgress);

      if (this.isCollectionOnHold && !itemInProgress) {
        this.restartUploading.emit();
      }

      const examples = this.collectionItems.filter(
        item => item['totalElements'] === undefined && item.status === CircleLoadingProgressEnum.Uploaded
      );

      this.uploadedExamples = this.collectionItems.filter(item => item.status === CircleLoadingProgressEnum.Uploaded);

      this.uploadedExamples.length && this.collectionItems[0]['totalElements']
        ? (this.totalElements = examples.length + this.collectionItems[0]['totalElements'])
        : (this.totalElements = examples.length || 0);
    }
  }

  onScrollDown(): void {
    const lastItem = this.uploadedExamples[this.uploadedExamples.length - 1];
    const nextPage = lastItem['page'] + 1;
    const totalPages = lastItem['totalPages'];

    if (totalPages !== nextPage && !isNaN(nextPage)) {
      this.prevItemCollection = this.collectionItems;
      this.loadMore.emit(lastItem);
    }
  }
}
