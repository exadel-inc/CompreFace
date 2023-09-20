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

import { Component, Inject, ChangeDetectionStrategy, OnInit, ChangeDetectorRef } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';
import { Observable, Subscription } from 'rxjs';
import { filter, takeWhile, tap } from 'rxjs/operators';
import { Role } from 'src/app/data/enums/role.enum';
import { AppUser } from 'src/app/data/interfaces/app-user';
import { UserData } from 'src/app/data/interfaces/user-data';
import { BreadcrumbsFacade } from '../breadcrumbs/breadcrumbs.facade';
import { DeleteDialogComponent } from '../delete-dialog/delete-dialog.component';
import { InviteDialogComponent } from '../invite-dialog/invite-dialog.component';
import { Router } from '@angular/router';
import { Routes } from 'src/app/data/enums/routers-url.enum';
import { ApplicationListFacade } from '../application-list/application-list-facade';

@Component({
  selector: 'manage-app-users',
  templateUrl: './manage-app-users.component.html',
  styleUrls: ['./manage-app-users.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ManageAppUsersDialog implements OnInit {
  availableRoles$: Observable<string[]>;
  availableEmails$: Observable<string[]>;

  collection: UserData[];
  appOwner: UserData;
  selectedUser: UserData;

  roleValues: string[];
  role = Role;
  availableRoles: string[];
  search: string = '';
  availableRolesSubscription: Subscription;

  constructor(
    public dialogRef: MatDialogRef<ManageAppUsersDialog>,
    public dialog: MatDialog,
    private readonly cdRef: ChangeDetectorRef,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private breadcrumbsFacade: BreadcrumbsFacade,
    private applicationListFacade: ApplicationListFacade,
    private translate: TranslateService,
    private router: Router
  ) {
    this.availableEmails$ = this.breadcrumbsFacade.availableEmails$;
    this.availableRoles$ = this.breadcrumbsFacade.availableRoles$;
    this.data.collection.subscribe((collection: AppUser[]) => this.sortUsers(collection));
  }

  ngOnInit(): void {
    this.roleValues = Object.keys(Role);
    this.availableRolesSubscription = this.breadcrumbsFacade.availableRoles$.subscribe(value => (this.availableRoles = value));
  }

  sortUsers(collection: AppUser[]): void {
    const usersCollection = collection.map(user => {
      return {
        role: user.role,
        userId: user.id,
        email: user.email,
        fullName: user.firstName + ' ' + user.lastName,
      };
    });

    this.appOwner = usersCollection.find(user => user.role === Role.Owner);

    const administrators = usersCollection
      .filter(user => user.role === Role.Administrator)
      .sort((user, next) => user.fullName.localeCompare(next.fullName));

    const users = usersCollection.filter(user => user.role === Role.User).sort((user, next) => user.fullName.localeCompare(next.fullName));

    this.collection = [this.appOwner, ...administrators, ...users];

    this.cdRef.markForCheck();
  }

  onChange(user: UserData, newRole: string): void {
    const role = newRole.toUpperCase();
    this.selectedUser = null;
    this.breadcrumbsFacade.updateUserRole(user.userId, role as Role, this.data.currentApp.id);
  }

  onDropdown(event: Event, index: number): void {
    event.stopPropagation();
    if (this.selectedUser?.userId === this.collection[index].userId) {
      this.selectedUser = null;
      return;
    }
    this.selectedUser = this.collection[index];
  }

  onCloseDropdown() {
    if (!this.selectedUser) return;

    this.selectedUser = null;
  }

  onDelete(user: UserData): void {
    const dialog = this.dialog.open(DeleteDialogComponent, {
      panelClass: 'custom-mat-dialog',
      data: {
        entityType: this.translate.instant('users.user'),
      },
    });

    const dialogSubs = dialog
      .afterClosed()
      .pipe(
        filter(data => data),
        tap(() => {
          this.breadcrumbsFacade.deleteAppUsers(user.userId, this.data.currentApp.id);
          if (this.data.currentUserId === user.userId) {
            this.applicationListFacade.loadApplications();
            this.router.navigate([Routes.Home]);
            this.dialog.closeAll();
          }
        })
      )
      .subscribe(() => dialogSubs.unsubscribe());
  }

  onInvite(): void {
    const dialog = this.dialog.open(InviteDialogComponent, {
      panelClass: 'custom-mat-dialog',
      data: {
        availableRoles: this.availableRoles,
        options$: this.availableEmails$,
      },
    });

    const dialogSubs = dialog
      .afterClosed()
      .pipe(
        takeWhile(({ userEmail, role }) => userEmail && role),
        tap(({ userEmail, role }) => {
          this.breadcrumbsFacade.inviteUser(userEmail, role, this.data.currentApp.id);
        })
      )
      .subscribe(() => dialogSubs.unsubscribe());
  }
}
