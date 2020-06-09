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

import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material';
import { Observable, of, Subscription } from 'rxjs';
import { catchError, filter, map, switchMap, take, tap } from 'rxjs/operators';
import { AppUser } from 'src/app/data/appUser';

import { DeleteDialogComponent } from '../delete-dialog/delete-dialog.component';
import { InviteDialogComponent } from '../invite-dialog/invite-dialog.component';
import { SnackBarService } from '../snackbar/snackbar.service';
import { ITableConfig } from '../table/table.component';
import { UserListFacade } from './user-list-facade';
import { selectUserId } from 'src/app/store/userInfo/selectors';


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
  errorMessage: string;
  search = '';
  availableRolesSubscription: Subscription;
  currentUserId$: Observable<string>;

  constructor(private userListFacade: UserListFacade, private snackBarService: SnackBarService, public dialog: MatDialog) {
    userListFacade.initSubscriptions();
  }

  ngOnInit() {
    this.isLoading$ = this.userListFacade.isLoading$;
    this.userRole$ = this.userListFacade.userRole$;

    this.tableConfig$ = this.userListFacade.users$.pipe(map((users: AppUser[]) => {
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
  }

  onChange(user: AppUser): void {
    this.userListFacade.updateUserRole(user.id, user.role);
  }

  onDelete(user: AppUser): void {
    this.userListFacade.selectedOrganizationName$
      .pipe(
        take(1),
        switchMap((name: string) => {
          return this.dialog.open(DeleteDialogComponent, {
            width: '400px',
            data: {
              entityType: 'User',
              entityName: `${user.firstName} ${user.lastName}`,
              organizationName: name
            }
          }).afterClosed();
        }),
        filter((isClosed: boolean) => isClosed),
        tap(() => this.userListFacade.deleteUser(user.userId))

      )
      .subscribe();
  }

  onInviteUser(): void {
    const dialog = this.dialog.open(InviteDialogComponent, {
      data: {
        availableRoles: this.availableRoles
      }
    });

    let userEmailValue: string = '';

    dialog.afterClosed()
      .pipe(
        filter((data: any) => !!data),
        tap(({ userEmail }) => userEmailValue = userEmail),
        switchMap(({ userEmail, role }) => this.userListFacade.inviteUser(userEmail, role)),
        tap(() => this.openEmailNotification(userEmailValue)),
        catchError((error: HttpErrorResponse) => of(this.snackBarService.openHttpError(error))),
      )
      .subscribe()
  }

  ngOnDestroy(): void {
    this.userListFacade.unsubscribe();
    this.availableRolesSubscription.unsubscribe();
  }

  private openEmailNotification(email: string): void {
    if (!email) {
      return;
    }

    this.snackBarService.openInfo(void 0, void 0, `Invitation was sent to ${email}`);
  }
}
