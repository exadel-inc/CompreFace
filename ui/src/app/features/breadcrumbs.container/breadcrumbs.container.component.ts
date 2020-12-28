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
import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';

import { Application } from '../../data/interfaces/application';
import { Model } from '../../data/interfaces/model';
import { BreadcrumbsFacade } from '../breadcrumbs/breadcrumbs.facade';

@Component({
  selector: 'app-breadcrumbs-container',
  template: ` <app-breadcrumbs [model]="model$ | async" [app]="app$ | async"> </app-breadcrumbs>`,
  styleUrls: ['./breadcrumbs.container.component.scss'],
})
export class BreadcrumbsContainerComponent implements OnInit {
  app$: Observable<Application>;
  model$: Observable<Model>;

  constructor(private breadcrumbsFacade: BreadcrumbsFacade) {}

  ngOnInit() {
    this.app$ = this.breadcrumbsFacade.app$;
    this.model$ = this.breadcrumbsFacade.model$;
  }
}
