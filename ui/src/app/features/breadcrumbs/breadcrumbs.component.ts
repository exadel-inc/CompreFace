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
import {Component, OnDestroy, OnInit} from '@angular/core';
import {Observable, Subscription} from 'rxjs';

import {Application} from '../../data/application';
import {ROUTERS_URL} from '../../data/routers-url.variable';
import {BreadcrumbsFacade} from './breadcrumbs.facade';
import {Model} from "../../data/model";

@Component({
  selector: 'app-breadcrumbs',
  templateUrl: './breadcrumbs.component.html',
  styleUrls: ['./breadcrumbs.component.scss']
})
export class BreadcrumbsComponent implements OnInit, OnDestroy {
  orgSubscription: Subscription;
  orgId: string;
  app$: Observable<Application>;
  model$: Observable<Model>;
  ROUTERS_URL = ROUTERS_URL;
  maxNameLength = 20;

  constructor(private breadcrumbsFacade: BreadcrumbsFacade) {
  }

  ngOnInit(): void {
    this.orgSubscription = this.breadcrumbsFacade.orgId$.subscribe(orgId => this.orgId = orgId);
    this.app$ = this.breadcrumbsFacade.app$;
    this.model$ = this.breadcrumbsFacade.model$;
  }

  ngOnDestroy(): void {
    this.orgSubscription.unsubscribe();
  }
}
