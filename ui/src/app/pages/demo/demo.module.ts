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
import { TranslateModule } from '@ngx-translate/core';
import { FaceRecognitionModule } from '../../features/face-recognition/face-recognition.module';
import { SpinnerModule } from '../../features/spinner/spinner.module';
import { DemoComponent } from './demo.component';
import { RouterModule } from '@angular/router';
import { DemoService } from './demo.service';
import { DemoGuard } from './demo.guard';

@NgModule({
  declarations: [DemoComponent],
  imports: [
    CommonModule,
    RouterModule.forChild([{ path: '', component: DemoComponent, canActivate: [DemoGuard] }]),
    TranslateModule,
    FaceRecognitionModule,
    SpinnerModule,
  ],
  providers: [DemoService],
})
export class DemoModule {}
