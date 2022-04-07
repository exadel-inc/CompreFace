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
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';

import { TruncateModule } from '../../ui/truncate-pipe/truncate.module';
import { InviteDialogComponent } from '../invite-dialog/invite-dialog.component';
import { InviteDialogModule } from '../invite-dialog/invite-dialog.module';
import { BreadcrumbsComponent } from './breadcrumbs.component';
import { BreadcrumbsFacade } from './breadcrumbs.facade';

@NgModule({
  declarations: [BreadcrumbsComponent],
  exports: [BreadcrumbsComponent],
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    TruncateModule,
    MatTooltipModule,
    TranslateModule,
    MatIconModule,
    InviteDialogModule,
  ],
  providers: [BreadcrumbsFacade],
  entryComponents: [InviteDialogComponent],
})
export class BreadcrumbsModule {}
