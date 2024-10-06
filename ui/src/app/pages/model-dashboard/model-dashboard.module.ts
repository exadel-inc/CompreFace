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
import { RouterModule } from '@angular/router';

import { ModelDashboardComponent } from './model-dashboard.component';
import { BreadcrumbsContainerModule } from '../../features/breadcrumbs.container/breadcrumbs.container.module';
import { ModelInfoModule } from 'src/app/features/model-info/model-info.module';
import { ManageCollectionPageService } from '../manage-collection/manage-collection.service';

@NgModule({
  declarations: [ModelDashboardComponent],
  imports: [
    CommonModule,
    RouterModule.forChild([
      {
        path: '',
        component: ModelDashboardComponent,
      },
    ]),
    BreadcrumbsContainerModule,
    ModelInfoModule,
  ],
  providers: [ManageCollectionPageService],
})
export class ModelDashboardModule {}
