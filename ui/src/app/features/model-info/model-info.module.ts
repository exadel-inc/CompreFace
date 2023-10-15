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
import { ModelInfoComponent } from './model-info.component';
import { ModelInfoFacade } from './model-info.facade';
import { ClipboardModule } from '@angular/cdk/clipboard';
import { TruncateModule } from 'src/app/ui/truncate-pipe/truncate.module';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ModelStatisticsModule } from '../model-statistics/model-statistics.module';

@NgModule({
  declarations: [ModelInfoComponent],
  imports: [
    CommonModule,
    TranslateModule,
    MatButtonModule,
    MatIconModule,
    ClipboardModule,
    MatTooltipModule,
    TruncateModule,
    ModelStatisticsModule,
  ],
  exports: [ModelInfoComponent],
  providers: [ModelInfoFacade],
})
export class ModelInfoModule {}
