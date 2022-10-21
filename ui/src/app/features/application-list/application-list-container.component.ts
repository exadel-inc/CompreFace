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
import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { Observable, Subscription } from 'rxjs';
import { AppUser } from 'src/app/data/interfaces/app-user';
import { Application } from 'src/app/data/interfaces/application';
import { CreateDialogComponent } from 'src/app/features/create-dialog/create-dialog.component';

import { Routes } from '../../data/enums/routers-url.enum';
import { ManageUsersDialog } from '../manage-users-dialog/manage-users.component';
import { ApplicationListFacade } from './application-list-facade';

@Component({
  selector: 'app-application-list-container',
  template: `
    <app-application-list
      [isLoading]="isLoading$ | async"
      [userRole]="userRole$ | async"
      [applicationCollection]="applications"
      (selectApp)="onClick($event)"
      (createApp)="onCreateNewApp()"
      (manageUsers)="onManageUsers()"
    >
    </app-application-list>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationListContainerComponent implements OnInit, OnDestroy {
  isLoading$: Observable<boolean>;
  userRole$: Observable<string>;
  users$: Observable<AppUser[]>;
  currentUserId$: Observable<string>;

  applications: Application[];
  subs: Subscription;

  constructor(
    private applicationFacade: ApplicationListFacade,
    private dialog: MatDialog,
    private router: Router,
    private translate: TranslateService
  ) {
    this.applicationFacade.initSubscriptions();
  }

  ngOnInit() {
    this.isLoading$ = this.applicationFacade.isLoading$;
    this.userRole$ = this.applicationFacade.userRole$;
    this.users$ = this.applicationFacade.appUsers$;
    this.currentUserId$ = this.applicationFacade.currentUserId$;
    this.subs = this.applicationFacade.applications$.subscribe(applications => (this.applications = applications));
  }

  onClick(application): void {
    this.router.navigate([Routes.Application], {
      queryParams: {
        app: application.id,
      },
    });
  }

  onCreateNewApp(): void {
    const applicationNames = this.applications.map(app => app.name);

    const dialog = this.dialog.open(CreateDialogComponent, {
      panelClass: 'custom-mat-dialog',
      data: {
        entityType: this.translate.instant('applications.header.title'),
        placeholder: this.translate.instant('applications.name'),
        errorMsg: this.translate.instant('applications.error_msg'),
        nameList: applicationNames,
        name: '',
      },
    });

    const dialogSubscription = dialog.afterClosed().subscribe(name => {
      if (name) {
        this.applicationFacade.createApplication(name);
        dialogSubscription.unsubscribe();
      }
    });
  }

  onManageUsers() {
    let userCollection;
    let userId;
    let userRole;

    const userSubs = this.users$.subscribe(res => (userCollection = res));

    const userIdSubs = this.currentUserId$.subscribe(res => (userId = res));

    const userRoleSubs = this.userRole$.subscribe(res => (userRole = res));

    const dialog = this.dialog.open(ManageUsersDialog, {
      data: {
        collection: this.users$,
        currentUserId: userId,
        currentUserRole: userRole,
        applications: this.applications,
      },
    });

    const dialogSubs = dialog.afterClosed().subscribe(() => {
      userSubs.unsubscribe();
      userIdSubs.unsubscribe();
      userRoleSubs.unsubscribe();
    });
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }
}
