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

import {Injectable} from '@angular/core';
import {OrganizationEnService} from '../../store/organization/organization-entitys.service';
import {
  selectCurrentOrganizationId,
  selectSelectedOrganization,
  selectUserRollForSelectedOrganization
} from '../../store/organization/selectors';
import {Store} from '@ngrx/store';
import {Observable, Subscription} from 'rxjs';
import {Organization} from '../../data/organization';
import {AppState} from '../../store';
import {setSelectedId} from '../../store/organization/action';
import {ROUTERS_URL} from '../../data/routers-url.variable';
import {Router} from '@angular/router';
import {IFacade} from '../../data/facade/IFacade';
import {filter, map, take} from 'rxjs/operators';

@Injectable()
export class OrganizationHeaderFacade implements IFacade {
  selectedId$: Observable<string | null>;
  selectedId: string | null;
  public userRole$: Observable<string | null>;
  public organizations$: Observable<Organization[]>;
  public organizationName$: Observable<string>;
  selectedIdSub: Subscription;

  constructor(private organizationEnService: OrganizationEnService, private store: Store<AppState>, private router: Router) {
    this.organizations$ = this.organizationEnService.entities$;
    this.userRole$ = this.store.select(selectUserRollForSelectedOrganization);
    this.selectedId$ = this.store.select(selectCurrentOrganizationId);
    this.organizationName$ = this.store.select(selectSelectedOrganization).pipe(
      filter(org => !!org),
      map(org => org.name)
    );
  }

  initSubscriptions() {
    this.selectedIdSub = this.selectedId$.subscribe(id => {
      this.selectedId = id;
    });
  }

  unsubscribe() {
    this.selectedIdSub.unsubscribe();
  }

  select(id: string) {
    this.store.dispatch(setSelectedId({ selectId: id }));
    this.router.navigate([ROUTERS_URL.ORGANIZATION, id]);
  }

  rename(name: string) {
    this.organizationEnService.update({name, id: this.selectedId});
  }

  delete() {
    this.organizationEnService.delete(this.selectedId);

    let orgId = '';
    this.organizations$.pipe(
      take(1),
      map(org => org[0].id),
    ).subscribe(id => orgId = id);

    this.store.dispatch(setSelectedId({ selectId: orgId }));
    this.router.navigate([ROUTERS_URL.ORGANIZATION, orgId]);
  }

  add(org) {
    this.organizationEnService.add(org).subscribe(responce => {
      if (responce) {
        this.store.dispatch(setSelectedId({selectId: responce.id}));
        this.router.navigate([ROUTERS_URL.ORGANIZATION, responce.id]);
      }
    });
  }
}
