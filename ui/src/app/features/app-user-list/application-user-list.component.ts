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
import { TranslateService } from '@ngx-translate/core';
import { Observable, Subscription } from 'rxjs';
import { filter, first, map, takeWhile, tap } from 'rxjs/operators';
import { Role } from 'src/app/data/enums/role.enum';
import { AppUser } from 'src/app/data/interfaces/app-user';
import { Application } from 'src/app/data/interfaces/application';

import { UserDeletion } from '../../data/interfaces/user-deletion';
import { DeleteDialogComponent } from '../delete-dialog/delete-dialog.component';
import { InviteDialogComponent } from '../invite-dialog/invite-dialog.component';
import { ITableConfig } from '../table/table.component';
import { ApplicationUserListFacade } from './application-user-list-facade';

@Component({
  selector: 'app-application-user-list',
  templateUrl: './application-user-list.component.html',
  styleUrls: ['./application-user-list.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationUserListComponent implements OnInit, OnDestroy {
  tableConfig$: Observable<ITableConfig>;
  isLoading$: Observable<boolean>;
  userRole$: Observable<string>;
  availableRoles$: Observable<string[]>;
  availableEmails$: Observable<string[]>;
  message: string;
  search = '';
  availableRoles: string[];
  currentUserId$: Observable<string>;
  currentApp$: Observable<Application>;
  roleEnum = Role;
  availableRolesSubscription: Subscription;

  constructor(private appUserListFacade: ApplicationUserListFacade, private dialog: MatDialog, private translate: TranslateService) {
    appUserListFacade.initSubscriptions();
  }

  ngOnInit() {
    this.isLoading$ = this.appUserListFacade.isLoading$;
    this.userRole$ = this.appUserListFacade.userRole$;
    this.availableEmails$ = this.appUserListFacade.availableEmails$;
    this.currentUserId$ = this.appUserListFacade.currentUserId$;
    this.currentApp$ = this.appUserListFacade.selectedApplication$;

    this.tableConfig$ = this.appUserListFacade.appUsers$.pipe(
      map((users: AppUser[]) => ({
        columns: [
          { title: 'user', property: 'username' },
          { title: 'email', property: 'userEmail' },
          { title: 'role', property: 'role' },
          { title: 'delete', property: 'delete' },
        ],
        data: users,
      }))
    );
    this.message = this.translate.instant('app_users.add_users_info');
    this.availableRoles$ = this.appUserListFacade.availableRoles$;
    this.availableRolesSubscription = this.appUserListFacade.availableRoles$.subscribe(value => (this.availableRoles = value));
  }

  onChange(user: AppUser): void {
    this.appUserListFacade.updateUserRole(user.id, user.role);
  }

  onSearch(value: string) {
    this.search = value;
  }

  onDelete(deletion: UserDeletion): void {
    const dialog = this.dialog.open(DeleteDialogComponent, {
      panelClass: 'custom-mat-dialog',
      data: {
        entityType: this.translate.instant('users.user'),
        entityName: `${deletion.userToDelete.firstName} ${deletion.userToDelete.lastName}`,
        applicationName: this.appUserListFacade.selectedApplicationName,
      },
    });

    dialog
      .afterClosed()
      .pipe(
        first(),
        filter(result => result)
      )
      .subscribe(() => this.appUserListFacade.delete(deletion.userToDelete.userId));
  }

  ngOnDestroy(): void {
    this.appUserListFacade.unsubscribe();
    this.availableRolesSubscription.unsubscribe();
  }

  onInviteUser(): void {
    const dialog = this.dialog.open(InviteDialogComponent, {
      panelClass: 'custom-mat-dialog',
      data: {
        availableRoles: this.availableRoles,
        options$: this.availableEmails$,
      },
    });

    dialog
      .afterClosed()
      .pipe(takeWhile(({ userEmail, role }) => userEmail && role))
      .subscribe(({ userEmail, role }) => {
        this.appUserListFacade.inviteUser(userEmail, role);
      });
  }
}
