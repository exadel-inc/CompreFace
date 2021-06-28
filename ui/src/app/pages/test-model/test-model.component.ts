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
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';

import { selectPendingModel } from '../../store/model/selectors';
import { TestModelPageService } from './test-model.service';
import { ServiceTypes } from '../../data/enums/service-types.enum';

@Component({
  selector: 'app-test-model',
  templateUrl: './test-model.component.html',
  styleUrls: ['./test-model.component.scss'],
})
export class TestModelComponent implements OnInit, OnDestroy {
  modelLoading$: Observable<boolean>;
  type: ServiceTypes;
  verification: string = ServiceTypes.Verification;

  constructor(private modelService: TestModelPageService, private store: Store<any>) {}

  ngOnInit() {
    this.modelService.initUrlBindingStreams();
    this.modelLoading$ = this.store.select(selectPendingModel);
    this.type = this.modelService.getServiceType();
  }

  ngOnDestroy(): void {
    this.modelService.clearSelectedModelId();
    this.modelService.unSubscribe();
  }
}
