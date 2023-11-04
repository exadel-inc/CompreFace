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
import { EventEmitter, SimpleChanges } from '@angular/core';
import { Component, Input, Output, ChangeDetectionStrategy, OnChanges } from '@angular/core';
import { CircleLoadingProgressEnum } from 'src/app/data/enums/circle-loading-progress.enum';
import { CollectionItem } from 'src/app/data/interfaces/collection';

@Component({
  selector: 'image-holder',
  templateUrl: './image-holder.component.html',
  styleUrls: ['./image-holder.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ImageHolderComponent implements OnChanges {
  @Input() item: CollectionItem;
  @Input() selectionMode: CollectionItem;

  isDeleteVisible: boolean;

  @Output() onDelete = new EventEmitter<CollectionItem>();
  @Output() onCancel = new EventEmitter<CollectionItem>();
  @Output() onSelect = new EventEmitter<CollectionItem>();

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.item?.currentValue) {
      this.isDeleteVisible =
        this.item.status === CircleLoadingProgressEnum.Uploaded || this.item.status === CircleLoadingProgressEnum.Failed;
    }
  }

  onItemClick() {
    if (this.selectionMode) {
      this.onSelect.emit(this.item);
    }
  }
}
