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
import { CollectionManagerSubjectLeftComponent } from './collection-manager-subject-left/collection-manager-subject-left.component';
import { MatListModule } from '@angular/material/list';
import { TranslateModule } from '@ngx-translate/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialogModule } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AppSearchTableModule } from '../app-search-table/app-search-table.module';
import { CollectionLeftFacade } from './collection-left-facade';
import { SpinnerModule } from '../spinner/spinner.module';
import { TablePipeModule } from '../../ui/search-pipe/table-filter.module';
import { CollectionManagerSubjectLeftContainerComponent } from './collection-manager-subject-left.container.component';
import { CollectionRightFacade } from '../collection-manager-subject-right/collection-manager-right-facade';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog.component';
import { TruncateModule } from 'src/app/ui/truncate-pipe/truncate.module';

@NgModule({
  declarations: [CollectionManagerSubjectLeftContainerComponent, CollectionManagerSubjectLeftComponent, ConfirmDialogComponent],
  imports: [
    CommonModule,
    MatListModule,
    TranslateModule,
    MatButtonModule,
    MatIconModule,
    AppSearchTableModule,
    SpinnerModule,
    TablePipeModule,
    MatDialogModule,
    MatTooltipModule,
    TruncateModule,
  ],
  exports: [CollectionManagerSubjectLeftContainerComponent],
  providers: [CollectionLeftFacade, CollectionRightFacade],
})
export class CollectionManagerSubjectLeftModule {}
