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
import { TranslateModule } from '@ngx-translate/core';
import { SpinnerModule } from 'src/app/features/spinner/spinner.module';

import { ModelTableModule } from '../model-table/model-table.module';
import { ModelListFacade } from './model-list-facade';
import { ModelListComponent } from './model-list.component';
import { TablePipeModule } from '../../ui/search-pipe/table-filter.module';
import { FormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { AppSearchTableModule } from '../app-search-table/app-search-table.module';

@NgModule({
  declarations: [ModelListComponent],
  exports: [ModelListComponent],
  providers: [ModelListFacade],
  imports: [
    CommonModule,
    SpinnerModule,
    MatButtonModule,
    MatIconModule,
    ModelTableModule,
    TranslateModule,
    TablePipeModule,
    FormsModule,
    MatInputModule,
    AppSearchTableModule,
  ],
})
export class ModelListModule {}
