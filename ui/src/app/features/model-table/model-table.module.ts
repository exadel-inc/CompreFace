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
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslateModule } from '@ngx-translate/core';

import { TruncateModule } from '../../ui/truncate-pipe/truncate.module';
import { ModelTableComponent } from './model-table.component';
import { ModelCreateDialogComponent } from '../mode-create-dialog/model-create-dialog.component';
import { ModelCloneDialogComponent } from '../model-clone-dialog/model-clone-dialog.component';
import { MatFormFieldModule } from '@angular/material/form-field';
import { FormsModule } from '@angular/forms';
import { MatOptionModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';
import { MatDialogModule } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { ModelSortPipe } from './model-sort.pipe';
import { ClipboardModule } from '@angular/cdk/clipboard';

@NgModule({
  declarations: [ModelTableComponent, ModelSortPipe, ModelCreateDialogComponent, ModelCloneDialogComponent],
  exports: [ModelTableComponent],
  imports: [
    CommonModule,
    MatButtonModule,
    MatTableModule,
    MatIconModule,
    MatTooltipModule,
    MatMenuModule,
    TruncateModule,
    MatTooltipModule,
    TranslateModule,
    MatFormFieldModule,
    FormsModule,
    MatOptionModule,
    MatSelectModule,
    MatDialogModule,
    MatInputModule,
    ClipboardModule,
  ],
})
export class ModelTableModule {}
