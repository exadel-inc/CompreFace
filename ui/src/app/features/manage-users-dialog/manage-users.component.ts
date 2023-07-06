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

import { Component, Inject, ChangeDetectionStrategy, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { filter, tap } from 'rxjs/operators';
import { Role } from 'src/app/data/enums/role.enum';
import { AppUser } from 'src/app/data/interfaces/app-user';
import { UserData } from 'src/app/data/interfaces/user-data';
import { UserDeletion } from 'src/app/data/interfaces/user-deletion';
import { ApplicationListFacade } from '../application-list/application-list-facade';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog.component';
import { DeleteDialogComponent } from '../delete-dialog/delete-dialog.component';

@Component({
  selector: 'manage-users',
  templateUrl: './manage-users.component.html',
  styleUrls: ['./manage-users.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ManageUsersDialog implements OnDestroy {
  collection: UserData[];
  appUsers: AppUser[];

  currentUserData: AppUser;
  isBaseUser: boolean;
  owner: UserData;
  selectedUser: UserData = null;
  role = Role;

  subs: Subscription;
  disable: boolean = false;
  roleValues: string[];
  search: string = '';
  selectedOption = 'deleter';

  constructor(
    public confirmDialog: MatDialog,
    private readonly cdRef: ChangeDetectorRef,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private translate: TranslateService,
    private dialogRef: MatDialogRef<ManageUsersDialog>,
    private applicationFacade: ApplicationListFacade
  ) {
    this.subs = this.data.collection.subscribe((collection: AppUser[]) => this.sortUsers(collection));
  }

  onChange(user: UserData, newRole: string): void {
    this.selectedUser = null;
    const role = newRole.toUpperCase();
    if (newRole.toUpperCase() === Role.Owner) {
      const dialog = this.confirmDialog.open(ConfirmDialogComponent, {
        panelClass: 'custom-mat-dialog',
        data: {
          title: this.translate.instant('users.manage.role_update'),
          description: this.translate.instant('users.manage.role_update_description'),
        },
      });

      dialog
        .afterClosed()
        .pipe(
          filter(confirm => confirm),
          tap(() => this.applicationFacade.updateUserRole(user.userId, role as Role))
        )
        .subscribe();
      return;
    }

    this.applicationFacade.updateUserRole(user.userId, role as Role);
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

  sortUsers(collection: AppUser[]): void {
    this.appUsers = collection;
    const usersCollection = collection.map(user => {
      return {
        role: user.role,
        userId: user.id,
        email: user.email,
        fullName: user.firstName + ' ' + user.lastName,
      };
    });

    this.owner = usersCollection.find(user => user.role === Role.Owner);

    this.currentUserData = collection.find(user => user.userId === this.data.currentUserId);

    this.isBaseUser = this.currentUserData.role === Role.User;

    this.currentUserData.role === Role.Owner
      ? (this.roleValues = Object.keys(Role))
      : (this.roleValues = [Object.keys(Role)[1], Object.keys(Role)[2]]);

    const administrators = usersCollection
      .filter(user => user.role === Role.Administrator)
      .sort((user, next) => user.fullName.localeCompare(next.fullName));

    const users = usersCollection.filter(user => user.role === Role.User).sort((user, next) => user.fullName.localeCompare(next.fullName));

    this.collection = [this.owner, ...administrators, ...users];

    this.cdRef.markForCheck();
  }

  onDelete(user: UserData): void {
    const applicationsOwnedByUser = this.data.applications.filter(app => app.owner.userId === user.userId);

    const dialog = this.confirmDialog.open(DeleteDialogComponent, {
      panelClass: 'custom-mat-dialog',
      data: {
        entityType: this.translate.instant('users.user'),
        appList: applicationsOwnedByUser,
        user: user.fullName,
        owner: this.owner.userId === this.currentUserData.userId,
        isAppOwner: !!applicationsOwnedByUser.length,
      },
    });

    const dialogSubs = dialog
      .afterClosed()
      .pipe(
        filter(data => data),
        tap(() => {
          const isDeleteHimSelf = this.currentUserData.userId === user.userId;
          const userToDelete = this.appUsers.find(appUser => appUser.id === user.userId);
          const deletion: UserDeletion = {
            deleterUserId: this.currentUserData.userId,
            userToDelete: userToDelete,
            isDeleteHimSelf: isDeleteHimSelf,
          };
          this.applicationFacade.deleteUser(deletion, this.selectedOption);
          if (isDeleteHimSelf) {
            this.dialogRef.close();
          }
        })
      )
      .subscribe(() => dialogSubs.unsubscribe());
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }
}
