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
import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subscription } from 'rxjs';
import { ServiceTypes } from 'src/app/data/enums/service-types.enum';
import { Application } from 'src/app/data/interfaces/application';
import { CreateApplicationFacade } from '../../create-application.facade';

@Component({
  selector: 'create-service',
  templateUrl: './create-service.component.html',
  styleUrls: ['../../shared-styles.component.scss'],
})
export class CreateServiceComponent implements OnInit, OnDestroy {
  serviceTypes = [ServiceTypes.Recognition, ServiceTypes.Detection, ServiceTypes.Verification];
  service: FormGroup;
  appSubs: Subscription;
  currentApplication: Application;

  @Input() applicationName: string;

  constructor(private fb: FormBuilder, private appFacade: CreateApplicationFacade) { }

  ngOnInit() {
    this.appSubs = this.appFacade.applications$.subscribe(
      result => (this.currentApplication = result.find(app => app.name === this.applicationName))
    );

    this.service = this.fb.group({
      serviceName: ['', Validators.required],
      serviceType: [ServiceTypes.Recognition, Validators.required],
    });
  }

  onSave() {
    const serviceValue = this.service.value;
    const isFirstService = true;
    this.appFacade.createModel(
      serviceValue.serviceName,
      serviceValue.serviceType,
      this.currentApplication.id,
      isFirstService);
  }

  ngOnDestroy() {
    this.appSubs.unsubscribe();
  }
}
