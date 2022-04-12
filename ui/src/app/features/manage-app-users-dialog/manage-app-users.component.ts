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

import { Component, Inject, ChangeDetectionStrategy, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Observable, Subscription } from 'rxjs';
import { filter, takeWhile, tap } from 'rxjs/operators';
import { Role } from 'src/app/data/enums/role.enum';
import { AppUser } from 'src/app/data/interfaces/app-user';
import { UserData } from 'src/app/data/interfaces/user-data';
import { BreadcrumbsFacade } from '../breadcrumbs/breadcrumbs.facade';
import { DeleteDialogComponent } from '../delete-dialog/delete-dialog.component';
import { InviteDialogComponent } from '../invite-dialog/invite-dialog.component';

@Component({
  selector: 'manage-app-users',
  templateUrl: './manage-app-users.component.html',
  styleUrls: ['./manage-app-users.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ManageAppUsersDialog implements OnInit {
  availableRoles$: Observable<string[]>;
  availableEmails$: Observable<string[]>;

  deletedUsersCollection: UserData[] = [];
  updatedUsersCollection: UserData[] = [];
  collection: UserData[];
  appOwner: UserData;
  roleValues: string[];
  availableRoles: string[];
  search: string = '';
  closeSubs: Subscription;
  availableRolesSubscription: Subscription;

  constructor(
    public dialogRef: MatDialogRef<ManageAppUsersDialog>,
    public dialog: MatDialog,
    private readonly cdRef: ChangeDetectorRef,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private breadcrumbsFacade: BreadcrumbsFacade
  ) {
    this.availableEmails$ = this.breadcrumbsFacade.availableEmails$;
    this.availableRoles$ = this.breadcrumbsFacade.availableRoles$;
  }

  ngOnInit(): void {
    this.sortUsers(this.data.collection);
    this.appOwner = this.collection.find(user => user.role === Role.Owner);
    this.roleValues = Object.keys(Role);
    this.availableRolesSubscription = this.breadcrumbsFacade.availableRoles$.subscribe(value => (this.availableRoles = value));

    this.closeSubs = this.dialogRef.backdropClick().subscribe(() => this.onClose());
  }

  sortUsers(collection: AppUser[]): void {
    const usersCollection = collection
      .map(user => {
        return {
          role: user.role,
          userId: user.id,
          email: user.email,
          fullName: user.firstName + ' ' + user.lastName,
        };
      })
      .sort((user, next) => user.fullName.localeCompare(next.fullName));

    const owner = usersCollection.find(user => user.role === Role.Owner);

    const administrators = usersCollection
      .filter(user => user.role === Role.Administrator)
      .sort((user, next) => user.fullName.localeCompare(next.fullName));

    const users = usersCollection.filter(user => user.role === Role.User).sort((user, next) => user.fullName.localeCompare(next.fullName));

    this.collection = [owner, ...administrators, ...users];
  }

  onChange(user: UserData): void {
    const updatedUserData = this.data.collection.find(userData => user.userId === userData.userId);

    if (updatedUserData.role === user.role) {
      const index = this.updatedUsersCollection.indexOf(user);
      this.updatedUsersCollection.splice(index, 1);

      return;
    }
    this.updatedUsersCollection.push(user);
  }

  onDelete(user: UserData): void {
    const dialog = this.dialog.open(DeleteDialogComponent, {
      panelClass: 'custom-mat-dialog',
    });

    const dialogSubs = dialog
      .afterClosed()
      .pipe(
        filter(data => data),
        tap(() => {
          const index = this.collection.indexOf(user);

          this.deletedUsersCollection.push(this.collection[index]);

          this.collection.splice(index, 1);

          this.cdRef.markForCheck();
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

  onClose(): void {
    this.dialogRef.close({
      deletedUsers: this.deletedUsersCollection,
      updatedUsers: this.updatedUsersCollection,
      appId: this.data.currentApp.id,
    })
  }

  ngOnDestroy(): void {
    this.closeSubs.unsubscribe();
  }
}
