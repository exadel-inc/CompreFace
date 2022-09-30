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
import { Role } from 'src/app/data/enums/role.enum';
import { AppUser } from 'src/app/data/interfaces/app-user';
import { deleteUserFromApplication, inviteAppUser, updateAppUserRole } from 'src/app/store/app-user/action';
import { selectAppUsers, selectAvailableEmails, selectAvailableRoles } from 'src/app/store/app-user/selectors';
import { updateApplication, deleteApplication } from 'src/app/store/application/action';
import { selectImageCollection } from 'src/app/store/manage-collectiom/selectors';
import { selectCurrentUserRole, selectUsers } from 'src/app/store/user/selectors';
import { selectUserId } from 'src/app/store/userInfo/selectors';

import { Application } from '../../data/interfaces/application';
import { Model } from '../../data/interfaces/model';
import { AppState } from '../../store';
import { selectApplications, selectCurrentApp, selectCurrentAppId } from '../../store/application/selectors';
import { selectCurrentModel } from '../../store/model/selectors';

@Injectable()
export class BreadcrumbsFacade {
  app$: Observable<Application>;
  applications$: Observable<Application[]>;
  model$: Observable<Model>;
  selectedId$: Observable<string | null>;
  appUsers$: Observable<AppUser[]>;
  currentUserRole$: Observable<string>;
  organizationUsers$: Observable<AppUser[]>;
  availableRoles$: Observable<string[]>;
  availableEmails$: Observable<string[]>;
  currentUserId$: Observable<string>;
  selectedId: string | null;
  appIdSub: Subscription;
  collectionItems$: Observable<any>;

  constructor(private store: Store<AppState>) {
    this.app$ = this.store.select(selectCurrentApp);
    this.applications$ = this.store.select(selectApplications);
    this.model$ = this.store.select(selectCurrentModel);
    this.selectedId$ = this.store.select(selectCurrentAppId);
    this.appUsers$ = this.store.select(selectAppUsers);
    this.currentUserRole$ = this.store.select(selectCurrentUserRole);
    this.currentUserId$ = this.store.select(selectUserId);
    this.availableEmails$ = this.store.select(selectAvailableEmails);
    this.availableRoles$ = this.store.select(selectAvailableRoles);
    this.collectionItems$ = this.store.select(selectImageCollection);
  }

  rename(name: string, app: Application) {
    this.store.dispatch(updateApplication({ name, id: app.id }));
  }

  delete(app: Application) {
    this.store.dispatch(deleteApplication({ id: app.id }));
  }

  updateUserRole(id: string, role: Role, appId: string): void {
    this.store.dispatch(
      updateAppUserRole({
        applicationId: appId,
        user: {
          id,
          role,
        },
      })
    );
  }

  deleteAppUsers(userId: string, appId: string) {
    this.store.dispatch(
      deleteUserFromApplication({
        applicationId: appId,
        userId,
      })
    );
  }

  inviteUser(email: string, role: Role, appId: string): void {
    this.store.dispatch(
      inviteAppUser({
        applicationId: appId,
        userEmail: email,
        role,
      })
    );
  }
}
