/*!
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

import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslateModule } from '@ngx-translate/core';
import { TruncateModule } from 'src/app/ui/truncate-pipe/truncate.module';
import { CollectionManagerSubjectLeftModule } from '../collection-manager-subject-left/collection-manager-subject-left.module';
import { CollectionManagerSubjectRightModule } from '../collection-manager-subject-right/collection-manager-subject-right.module';
import { CollectionManagerComponent } from './collection-manager.components';

@NgModule({
  declarations: [CollectionManagerComponent],
  imports: [
    CommonModule,
    MatCardModule,
    MatTooltipModule,
    TranslateModule,
    CollectionManagerSubjectLeftModule,
    CollectionManagerSubjectRightModule,
    FormsModule,
    MatIconModule,
    TruncateModule,
  ],
  exports: [CollectionManagerComponent],
})
export class CollectionManagerModule {}
