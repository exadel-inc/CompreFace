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

import { Component, Inject, ChangeDetectionStrategy, OnInit, OnDestroy } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Subscription } from 'rxjs';
import { Role } from 'src/app/data/enums/role.enum';
import { AppUser } from 'src/app/data/interfaces/app-user';

export interface UserData {
  role: string;
  userId: string;
  firstName: string;
  lastName: string;
}

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

  closeSubs: Subscription;
  disable: boolean = false;
  roleValues: string[];
  search: string = '';

  constructor(public dialogRef: MatDialogRef<ManageUsersDialog>, @Inject(MAT_DIALOG_DATA) public data: any) {}

  ngOnInit(): void {
    this.currentUserData = this.data.userCollection.find(user => user.userId === this.data.currentUserId);

    this.currentUserData.role === Role.Owner
      ? (this.roleValues = Object.keys(Role))
      : (this.roleValues = [Object.keys(Role)[1], Object.keys(Role)[2]]);

    this.owner = this.data.userCollection.find(user => user.role === Role.Owner);

    this.collection = this.data.userCollection.map(user => {
      return {
        role: user.role,
        userId: user.id,
        firstName: user.firstName,
        lastName: user.lastName,
      };
    });

    this.closeSubs = this.dialogRef.backdropClick().subscribe(() =>
      this.dialogRef.close({
        deletedUsers: this.deletedUsersCollection,
        updatedUsers: this.updatedUsersCollection,
      })
    );
  }

  onChange(user: UserData): void {
    const updatedUserData = this.data.userCollection.find(userData => user.userId === userData.userId);

    if (updatedUserData.role === user.role) {
      const index = this.updatedUsersCollection.indexOf(user);
      this.updatedUsersCollection.splice(index, 1);

      return;
    }

    this.updatedUsersCollection.push(user);

    const ownerUser = this.updatedUsersCollection.find(user => user.role === Role.Owner);

    ownerUser ? (this.disable = true) : (this.disable = false);
  }

  onDelete(user: UserData): void {
    const index = this.collection.indexOf(user);

    this.deletedUsersCollection.push(this.collection[index]);

    this.collection.splice(index, 1);
  }

  ngOnDestroy(): void {
    this.closeSubs.unsubscribe();
  }
}
