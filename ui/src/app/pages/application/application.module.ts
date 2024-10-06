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
import { RouterModule } from '@angular/router';
import { BreadcrumbsContainerModule } from 'src/app/features/breadcrumbs.container/breadcrumbs.container.module';
import { ModelListModule } from 'src/app/features/model-list/model-list.module';

import { ApplicationHeaderModule } from '../../features/application-header/application-header.module';
import { ApplicationListModule } from '../../features/application-list/application-list.module';
import { BreadcrumbsModule } from '../../features/breadcrumbs/breadcrumbs.module';
import { FooterModule } from '../../features/footer/footer.module';
import { ToolBarModule } from '../../features/tool-bar/tool-bar.module';
import { ApplicationComponent } from './application.component';
import { ApplicationPageService } from './application.service';

@NgModule({
  declarations: [ApplicationComponent],
  imports: [
    CommonModule,
    MatButtonModule,
    ApplicationHeaderModule,
    ModelListModule,
    FooterModule,
    ApplicationListModule,
    ToolBarModule,
    BreadcrumbsModule,
    BreadcrumbsContainerModule,
    MatCardModule,
    RouterModule.forChild([{ path: '', component: ApplicationComponent }]),
  ],
  providers: [ApplicationPageService],
})
export class ApplicationModule {}
