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
import { Observable, Subject, combineLatest } from 'rxjs';
import { shareReplay, takeUntil } from 'rxjs/operators';
import { ServiceTypes } from 'src/app/data/enums/service-types.enum';
import { Model } from 'src/app/data/interfaces/model';
import { Statistics } from 'src/app/data/interfaces/statistics';
import { ModelInfoFacade } from './model-info.facade';

@Component({
  selector: 'model-info',
  templateUrl: './model-info.component.html',
  styleUrls: ['./model-info.component.scss'],
})
export class ModelInfoComponent implements OnInit, OnDestroy {
  currentModel: Model;
  recognition = ServiceTypes.Recognition;
  statistics$: Observable<Statistics[]>;
  unsubscribe$ = new Subject<void>();

  constructor(private modelInfoFacade: ModelInfoFacade) {
    this.statistics$ = this.modelInfoFacade.statistics$.pipe(shareReplay());
  }

  ngOnInit(): void {
    combineLatest([this.modelInfoFacade.selectCurrentAppId$, this.modelInfoFacade.selectCurrentModelId$])
      .pipe(takeUntil(this.unsubscribe$))
      .subscribe(([appId, modelId]) => {
        this.modelInfoFacade.loadModelStatistics(appId, modelId);
      });

    this.modelInfoFacade.statistics$.subscribe();

    this.modelInfoFacade.currentModel$.pipe(takeUntil(this.unsubscribe$)).subscribe(model => (this.currentModel = model));
  }

  ngOnDestroy(): void {
    this.unsubscribe$.next();
    this.unsubscribe$.complete();
  }
}
