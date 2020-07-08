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
import { MatDialog } from '@angular/material';
import { Observable, Subscription } from 'rxjs';
import { first, map } from 'rxjs/operators';
import { AppUser } from 'src/app/data/appUser';

import { DeleteDialogComponent } from '../delete-dialog/delete-dialog.component';
import { InviteDialogComponent } from '../invite-dialog/invite-dialog.component';
import { SnackBarService } from '../snackbar/snackbar.service';
import { ITableConfig } from '../table/table.component';
import { ApplicationUserListFacade } from './application-user-list-facade';

@Component({
  selector: 'app-application-user-list',
  templateUrl: './application-user-list.component.html',
  styleUrls: ['./application-user-list.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ApplicationUserListComponent implements OnInit, OnDestroy {
  appUsers$: Observable<AppUser[]>;
  isLoading$: Observable<boolean>;
  userRole$: Observable<string>;
  availableRoles$: Observable<string[]>;
  errorMessage: string;
  availableEmails$: Observable<string[]>;
  search = '';
  availableRoles: string[];
  currentUserId$: Observable<string>;
  private availableRolesSubscription: Subscription;

  constructor(
    private appUserListFacade: ApplicationUserListFacade,
    private dialog: MatDialog,
    private snackBarService: SnackBarService
  ) {
    appUserListFacade.initSubscriptions();
  }

  ngOnInit() {
    this.appUsers$ = this.appUserListFacade.appUsers$;
    this.isLoading$ = this.appUserListFacade.isLoading$;
    this.userRole$ = this.appUserListFacade.userRole$;
    this.availableEmails$ = this.appUserListFacade.availableEmails$;
    this.currentUserId$ = this.appUserListFacade.currentUserId$;
    this.availableRoles$ = this.appUserListFacade.availableRoles$;
    this.availableRolesSubscription = this.appUserListFacade.availableRoles$.subscribe(value => this.availableRoles = value);
  }

  onChange(user: AppUser): void {
    this.appUserListFacade.updateUserRole(user.id, user.role);
  }

  onDelete(user: AppUser): void {
    const dialog = this.dialog.open(DeleteDialogComponent, {
      width: '400px',
      data: {
        entityType: 'user',
        entityName: `${user.firstName} ${user.lastName}`,
        applicationName: this.appUserListFacade.selectedApplicationName,
      }
    });

    dialog.afterClosed().pipe(first()).subscribe(result => {
      if (result) {
        this.appUserListFacade.delete(user.userId);
      }
    });
  }

  ngOnDestroy(): void {
    this.appUserListFacade.unsubscribe();
    this.availableRolesSubscription.unsubscribe();
  }

  onInviteUser(): void {
    const dialog = this.dialog.open(InviteDialogComponent, {
      data: {
        availableRoles: this.availableRoles,
        options$: this.availableEmails$,
        actionType: 'add'
      }
    });

    const dialogSubscription = dialog.afterClosed().subscribe(({ userEmail, role }) => {
      if (userEmail && role) {
        this.appUserListFacade.inviteUser(userEmail, role).subscribe(() => this.openEmailNotification(userEmail));
        dialogSubscription.unsubscribe();
      }
    });
  }

  private openEmailNotification(email: string): void {
    if (!email) {
      return;
    }

    this.snackBarService.openInfo(void 0, void 0, `Invitation was sent to ${email}`);
  }
}
