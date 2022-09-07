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
import { TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { filter, tap } from 'rxjs/operators';
import { Role } from 'src/app/data/enums/role.enum';
import { AppUser } from 'src/app/data/interfaces/app-user';
import { UserData } from 'src/app/data/interfaces/user-data';
import { DeleteDialogComponent } from '../delete-dialog/delete-dialog.component';

@Component({
  selector: 'manage-users',
  templateUrl: './manage-users.component.html',
  styleUrls: ['./manage-users.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ManageUsersDialog implements OnInit, OnDestroy {
  deletedUsersCollection: UserData[] = [];
  updatedUsersCollection: UserData[] = [];
  collection: UserData[];

  currentUserData: AppUser;
  owner: AppUser;
  selectedUser: UserData = null;

  closeSubs: Subscription;
  disable: boolean = false;
  roleValues: string[];
  search: string = '';

  constructor(
    public dialogRef: MatDialogRef<ManageUsersDialog>,
    public confirmDialog: MatDialog,
    private readonly cdRef: ChangeDetectorRef,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    this.currentUserData = this.data.userCollection.find(user => user.userId === this.data.currentUserId);
    this.currentUserData.role === Role.Owner
      ? (this.roleValues = Object.keys(Role))
      : (this.roleValues = [Object.keys(Role)[1], Object.keys(Role)[2]]);

    this.owner = this.data.userCollection.find(user => user.role === Role.Owner);

    this.sortUsers();

    this.closeSubs = this.dialogRef.backdropClick().subscribe(() => this.onClose());
  }

  onChange(user: UserData, newRole: string): void {
    user.role = newRole.toUpperCase();
    this.selectedUser = null;
    const updatedUserData = this.data.userCollection.find(userData => user.userId === userData.userId);

    if (updatedUserData.role === user.role) {
      const index = this.updatedUsersCollection.indexOf(user);
      this.updatedUsersCollection.splice(index, 1);
      this.disableOption();

      return;
    }

    this.updatedUsersCollection.push(user);
    this.disableOption();
  }

  onDropdown(event: Event, index: number): void {
    event.stopPropagation();
    this.selectedUser = this.collection[index];
  }

  onCloseDropdown() {
    if (!this.selectedUser) return;

    this.selectedUser = null;
  }

  sortUsers(): void {
    const usersCollection = this.data.userCollection.map(user => {
      return {
        role: user.role,
        userId: user.id,
        email: user.email,
        fullName: user.firstName + ' ' + user.lastName,
      };
    });

    const owner = usersCollection.find(user => user.role === Role.Owner);

    const administrators = usersCollection
      .filter(user => user.role === Role.Administrator)
      .sort((user, next) => user.fullName.localeCompare(next.fullName));

    const users = usersCollection.filter(user => user.role === Role.User).sort((user, next) => user.fullName.localeCompare(next.fullName));

    this.collection = [owner, ...administrators, ...users];
  }

  onDelete(user: UserData): void {
    const dialog = this.confirmDialog.open(DeleteDialogComponent, {
      panelClass: 'custom-mat-dialog',
      data: {
        entityType: this.translate.instant('users.user'),
      },
    });

    dialog
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
      .subscribe();
  }

  disableOption(): void {
    if (this.currentUserData.userId === this.owner.userId) {
      const ownerUser = this.updatedUsersCollection.find(user => user.role === Role.Owner);

      ownerUser ? (this.disable = true) : (this.disable = false);
    }
  }

  onClose(): void {
    this.dialogRef.close({
      deletedUsers: this.deletedUsersCollection,
      updatedUsers: this.updatedUsersCollection,
    });
  }

  ngOnDestroy(): void {
    this.closeSubs.unsubscribe();
  }
}
