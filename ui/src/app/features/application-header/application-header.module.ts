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
import { MatCardModule } from '@angular/material/card';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';

import { TruncateModule } from '../../ui/truncate-pipe/truncate.module';
import { EntityTitleModule } from '../entity-title/entity-title.module';
import { SpinnerModule } from '../spinner/spinner.module';
import { ApplicationHeaderComponent } from './application-header/application-header.component';
import { ApplicationHeaderContainerComponent } from './application-header.container.component';
import { ApplicationHeaderFacade } from './application-header.facade';
import { MatIconModule } from '@angular/material/icon';

@NgModule({
  declarations: [ApplicationHeaderComponent, ApplicationHeaderContainerComponent],
  exports: [ApplicationHeaderComponent, ApplicationHeaderContainerComponent],
  imports: [
    CommonModule,
    RouterModule,
    MatButtonModule,
    SpinnerModule,
    EntityTitleModule,
    MatCardModule,
    TruncateModule,
    MatTooltipModule,
    TranslateModule,
    MatIconModule,
  ],
  providers: [ApplicationHeaderFacade],
})
export class ApplicationHeaderModule {}
