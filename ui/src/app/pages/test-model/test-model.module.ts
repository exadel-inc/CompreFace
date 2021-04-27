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
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { BreadcrumbsContainerModule } from 'src/app/features/breadcrumbs.container/breadcrumbs.container.module';

import { FaceServicesModule } from '../../features/face-services/face-services.module';
import { SpinnerModule } from '../../features/spinner/spinner.module';
import { TestModelComponent } from './test-model.component';
import { TestModelPageService } from './test-model.service';
import { ApplicationHeaderModule } from '../../features/application-header/application-header.module';

@NgModule({
  declarations: [TestModelComponent],
  imports: [
    CommonModule,
    BreadcrumbsContainerModule,
    RouterModule.forChild([{ path: '', component: TestModelComponent }]),
    MatCardModule,
    FaceServicesModule,
    SpinnerModule,
    TranslateModule,
    ApplicationHeaderModule,
  ],
  providers: [TestModelPageService],
})
export class TestModelModule {}
