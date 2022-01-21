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

import { Component, Inject, ChangeDetectionStrategy } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Subscription } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Role } from 'src/app/data/enums/role.enum';

export interface UserData {
  role: string;
  id: string;
  firstName: string;
  lastName: string;
}

@Component({
  selector: 'manage-users',
  templateUrl: './manage-users.component.html',
  styleUrls: ['./manage-users.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ManageUsersDialog {
  roleValues = Object.keys(this.data.userRoles);
  collection: UserData[];
  deletedUsersCollection: UserData[] = [];
  updatedUsersCollection: UserData[] = [];
  search: string = '';
  subs: Subscription;

  constructor(public dialogRef: MatDialogRef<ManageUsersDialog>, @Inject(MAT_DIALOG_DATA) public data: any) {}

  ngOnInit(): void {
    this.subs = this.data.currentUserRole
      .pipe(
        tap((role: string) => {
          if (role.toLocaleLowerCase() !== Role.Owner.toLocaleLowerCase()) {
            this.roleValues.shift();
          }
        })
      )
      .subscribe();

    this.collection = this.data.userCollection.map(user => {
      return {
        role: user.role,
        id: user.id,
        firstName: user.firstName,
        lastName: user.lastName,
      };
    });
  }

  onChange(user: UserData): void {
    const currenctUserData = this.data.userCollection.find(userData => user.id === userData.id);
    const updatedUserData = this.collection.find(userData => user.id === userData.id);

    if (currenctUserData.role === updatedUserData.role) {
      const index = this.updatedUsersCollection.indexOf(user);
      this.updatedUsersCollection.splice(index, 1);

      return;
    }

    this.updatedUsersCollection.push(user);
  }

  onDelete(user: UserData): void {
    const index = this.collection.indexOf(user);
    this.collection.splice(index, 1);

    this.deletedUsersCollection.push(this.collection[index]);
  }

  onSave(): void {
    const res = {
      deletedUsers: this.deletedUsersCollection,
      updatedUsers: this.updatedUsersCollection,
    };
    this.dialogRef.close(res);
    this.subs.unsubscribe();
  }
}
