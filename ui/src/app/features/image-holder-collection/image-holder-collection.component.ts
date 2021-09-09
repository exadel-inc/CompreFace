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
import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { CollectionItem } from 'src/app/data/interfaces/collection';

@Component({
  selector: 'image-holder-collection',
  templateUrl: './image-holder-collection.component.html',
  styleUrls: ['./image-holder-collection.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ImageHolderCollectionComponent {
  @Input() items: CollectionItem[] = [];
  @Input() selectionMode: boolean = false;

  @Output() deleteItem = new EventEmitter<CollectionItem>();
  @Output() cancelUploadItem = new EventEmitter<CollectionItem>();
  @Output() selectItem = new EventEmitter<CollectionItem>();
}
