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
import { MatStepperModule } from '@angular/material/stepper';
import { RouterModule } from '@angular/router';
import { CreateApplicationModule } from 'src/app/features/create-application/create-application.module';
import { CreateApplicationWizardComponent } from './create-application-wizard.component';
import { CreateApplicationGuard } from './create-application.guard';

@NgModule({
  declarations: [CreateApplicationWizardComponent],
  imports: [
    CommonModule,
    CreateApplicationModule,
    MatCardModule,
    MatStepperModule,
    RouterModule.forChild([{ path: '', component: CreateApplicationWizardComponent }]),
  ]
})
export class CreateApplicationWizardModule {}