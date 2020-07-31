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
import { filter, map, switchMap, take, tap } from 'rxjs/operators';
import { AppUser } from 'src/app/data/appUser';

import { DeleteDialogComponent } from '../delete-dialog/delete-dialog.component';
import { SnackBarService } from '../snackbar/snackbar.service';
import { ITableConfig } from '../table/table.component';
import { UserListFacade } from './user-list-facade';
import { RoleEnum } from 'src/app/data/roleEnum.enum';

@Component({
  selector: 'app-user-list-container',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UserListComponent implements OnInit, OnDestroy {
  tableConfig$: Observable<ITableConfig>;
  isLoading$: Observable<boolean>;
  userRole$: Observable<string>;
  availableRoles: string[];
  availableRoles$: Observable<string[]>;
  search = '';
  availableRolesSubscription: Subscription;
  currentUserId$: Observable<string>;
  currentUserEmail$: Observable<string>;
  seletedOption = 'deleter';
  orgOwnerEmail: string;

  constructor(private userListFacade: UserListFacade, private snackBarService: SnackBarService, public dialog: MatDialog) {
    userListFacade.initSubscriptions();
  }

  ngOnInit() {
    this.isLoading$ = this.userListFacade.isLoading$;
    this.userRole$ = this.userListFacade.userRole$;

    this.tableConfig$ = this.userListFacade.users$.pipe(map((users: AppUser[]) => {
      this.orgOwnerEmail = users.filter(user => user.role === RoleEnum.OWNER).map(user => user.email)[0];
      return {
        columns: [{ title: 'user', property: 'username' }, {
          title: 'role',
          property: 'role'
        }, { title: 'delete', property: 'delete' }],
        data: users
      };
    }));

    this.availableRoles$ = this.userListFacade.availableRoles$;
    this.availableRolesSubscription = this.userListFacade.availableRoles$.subscribe(value => this.availableRoles = value);
    this.currentUserId$ = this.userListFacade.currentUserId$;
    this.currentUserEmail$ = this.userListFacade.currentUserEmail$;
  }

  onChange(user: AppUser): void {
    this.userListFacade.updateUserRole(user.id, user.role);
  }

  onDelete(user: AppUser): void {
    this.userListFacade.currentUserEmail$
      .pipe(
        take(1),
        switchMap((email: string) => {
          return this.dialog.open(DeleteDialogComponent, {
            width: '400px',
            data: {
              entityType: 'system-user',
              entity: user,
              options: [
                { name: email, value: 'deleter' },
                { name: this.orgOwnerEmail, value: 'owner' },
              ],
              isOrganizationOwner: email === this.orgOwnerEmail,
              seletedOption: this.seletedOption,
            }
          }).afterClosed();
        }),
        filter((isClosed: boolean) => isClosed),
        tap(() => this.userListFacade.deleteUser(user.userId, this.seletedOption)),
      )
      .subscribe();
  }

  ngOnDestroy(): void {
    this.userListFacade.unsubscribe();
    this.availableRolesSubscription.unsubscribe();
  }
}
