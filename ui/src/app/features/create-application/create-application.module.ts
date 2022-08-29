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
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { TranslateModule } from '@ngx-translate/core';
import { CreateApplicationComponent } from './create-application.component';
import { MatStepperModule } from '@angular/material/stepper';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ApplicationNameComponent } from './application/application-name.component';
import { CreateServiceComponent } from './service/create-service/create-service.component';
import { ServiceInfoComponent } from './service/service-info/service-info.component';
import { CreateApplicationFacade } from './create-application.facade';

@NgModule({
  declarations: [CreateApplicationComponent, ApplicationNameComponent, CreateServiceComponent, ServiceInfoComponent],
  imports: [CommonModule, MatButtonModule, MatStepperModule, MatSelectModule, MatInputModule, TranslateModule, ReactiveFormsModule],
  providers: [CreateApplicationFacade],
  exports: [CreateApplicationComponent],
})
export class CreateApplicationModule {}
