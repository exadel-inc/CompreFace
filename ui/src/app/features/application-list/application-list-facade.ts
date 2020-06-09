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
import { Observable, Subscription } from 'rxjs';
import { Application } from 'src/app/data/application';
import { IFacade } from 'src/app/data/facade/IFacade';
import { AppState } from 'src/app/store';
import { createApplication, loadApplications } from 'src/app/store/application/action';
import { selectApplications, selectIsPendingApplicationList } from 'src/app/store/application/selectors';
import { selectCurrentOrganizationId, selectUserRollForSelectedOrganization } from 'src/app/store/organization/selectors';

@Injectable()
export class ApplicationListFacade implements IFacade {
  applications$: Observable<Application[]>;
  selectedOrganizationId$: Observable<string>;
  isLoading$: Observable<boolean>;
  userRole$: Observable<string>;

  private selectedOrganizationIdSubscription: Subscription;
  private selectedOrgId: string;

  constructor(private store: Store<AppState>) {
    this.applications$ = store.select(selectApplications);
    this.selectedOrganizationId$ = store.select(selectCurrentOrganizationId);
    this.userRole$ = this.store.select(selectUserRollForSelectedOrganization);

    this.isLoading$ = store.select(selectIsPendingApplicationList);
  }

  initSubscriptions(): void {
    this.selectedOrganizationIdSubscription = this.selectedOrganizationId$.subscribe(
      organizationId => {
        if (organizationId) {
          this.selectedOrgId = organizationId;
          this.loadApplications();
        }
      }
    );
  }

  loadApplications(): void {
    this.store.dispatch(
      loadApplications({ organizationId: this.selectedOrgId })
    );
  }

  createApplication(name: string): void {
    this.store.dispatch(
      createApplication({ organizationId: this.selectedOrgId, name })
    );
  }

  unsubscribe(): void {
    this.selectedOrganizationIdSubscription.unsubscribe();
  }

  getOrgId(): string {
    return this.selectedOrgId;
  }
}
