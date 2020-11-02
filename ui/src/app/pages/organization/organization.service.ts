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
import { Injectable } from '@angular/core';
import { Store } from '@ngrx/store';
import { combineLatest, Observable, Subscription } from 'rxjs';
import { filter, map } from 'rxjs/operators';

import { Organization } from '../../data/interfaces/organization';
import { AppState } from '../../store';
import { setSelectedId } from '../../store/organization/action';
import { OrganizationEnService } from '../../store/organization/organization-entitys.service';
import { selectCurrentOrganizationId } from '../../store/organization/selectors';
import { getUserInfo } from '../../store/userInfo/action';

@Injectable()
export class OrganizationService {
  selectedId$: Observable<string>;
  private subscription: Subscription;

  private organizations$: Observable<Array<Organization>>;

  constructor(
    private organizationEnService: OrganizationEnService,
    private store: Store<AppState>,
  ) { }

  initUrlBindingStreams() {
    this.organizationEnService.load();
    this.store.dispatch(getUserInfo());
    this.organizations$ = this.organizationEnService.entities$;
    this.selectedId$ = this.store.select(selectCurrentOrganizationId);

    this.subscription = combineLatest([this.selectedId$, this.organizations$]).pipe(
      filter(([selectedId, data]) => data.length && selectedId === null),
      map(([selectedId, data]) => data[0].id),
    ).subscribe(routerId => {
      this.store.dispatch(setSelectedId({ selectId: routerId }));
    });
  }

  unSubscribe() {
    this.subscription.unsubscribe();
    // clear selected Id for Organization
    this.store.dispatch(setSelectedId({ selectId: null }));
  }
}
