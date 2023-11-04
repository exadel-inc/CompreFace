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
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslateModule } from '@ngx-translate/core';
import { TablePipeModule } from 'src/app/ui/search-pipe/table-filter.module';
import { TruncateModule } from 'src/app/ui/truncate-pipe/truncate.module';
import { ApplicationCollectionContainerComponent } from './application-collection-container.component';
import { ApplicationCollectionComponent } from './application-collection/application-collection.component';

@NgModule({
  declarations: [ApplicationCollectionContainerComponent, ApplicationCollectionComponent],
  imports: [CommonModule, TranslateModule, TablePipeModule, MatIconModule,MatTooltipModule, TruncateModule],
  exports: [ApplicationCollectionContainerComponent, ApplicationCollectionComponent],
})
export class ApplicationCollectionContainerModule {}
