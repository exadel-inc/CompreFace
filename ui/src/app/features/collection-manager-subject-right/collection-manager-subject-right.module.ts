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
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CollectionManagerSubjectRightComponent } from './collection-manager-subject-right/collection-manager-subject-right.component';
import { CollectionManagerSubjectRightContainerComponent } from './collection-manager-subject-right.container.component';
import { CollectionRightFacade } from './collection-manager-right-facade';
import { SpinnerModule } from '../spinner/spinner.module';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { TranslateModule } from '@ngx-translate/core';
import { DragNDropModule } from '../drag-n-drop/drag-n-drop.module';
import { ImageHolderCollectionModule } from '../image-holder-collection/image-holder-collection.module';

import { InfiniteScrollModule } from 'ngx-infinite-scroll';
import { IntersectionObserverModule } from '@ng-web-apis/intersection-observer';

@NgModule({
  declarations: [CollectionManagerSubjectRightContainerComponent, CollectionManagerSubjectRightComponent],
  exports: [CollectionManagerSubjectRightContainerComponent],
  imports: [
    CommonModule,
    SpinnerModule,
    MatIconModule,
    MatButtonModule,
    TranslateModule,
    DragNDropModule,
    ImageHolderCollectionModule,
    InfiniteScrollModule,
    IntersectionObserverModule,
  ],
  providers: [CollectionRightFacade],
})
export class CollectionManagerSubjectRightModule {}
