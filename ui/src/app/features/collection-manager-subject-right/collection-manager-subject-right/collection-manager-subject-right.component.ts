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
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { SubjectModeEnum } from 'src/app/data/enums/subject-mode.enum';
import { CollectionItem } from 'src/app/data/interfaces/collection';

@Component({
  selector: 'app-collection-manager-subject-right',
  templateUrl: './collection-manager-subject-right.component.html',
  styleUrls: ['./collection-manager-subject-right.component.scss'],
})
export class CollectionManagerSubjectRightComponent {
  infiniteScrollDistance: number = 1;
  scrollWindow: boolean = false;
  prevItemCollection: CollectionItem[] = [];
  subjectModeEnum = SubjectModeEnum;

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

  onScrollDown(): void {
    const lastItem = this.collectionItems[this.collectionItems.length - 1];
    const nextPage = lastItem['page'] + 1;
    const totalPages = lastItem['totalPages'];

    if (totalPages !== nextPage && !isNaN(nextPage)) {
      this.prevItemCollection = this.collectionItems;
      this.loadMore.emit(lastItem);
    }
  }
}
