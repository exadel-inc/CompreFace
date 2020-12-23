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
import { filter, map, switchMap, take, tap } from 'rxjs/operators';
import { Role } from 'src/app/data/enums/role.enum';
import { AppUser } from 'src/app/data/interfaces/app-user';

import { UserDeletion } from '../../data/interfaces/user-deletion';
import { DeleteDialogComponent } from '../delete-dialog/delete-dialog.component';
import { SnackBarService } from '../snackbar/snackbar.service';
import { ITableConfig } from '../table/table.component';
import { UserListFacade } from './user-list-facade';

@Component({
  selector: 'app-user-list-container',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
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
  orgOwnerEmail: string;
  messageHeader: string;
  message: string;
  translate: TranslateService;

  constructor(
    private userListFacade: UserListFacade,
    private snackBarService: SnackBarService,
    public dialog: MatDialog,
    translate: TranslateService
  ) {
    userListFacade.initSubscriptions();
    this.translate = translate;
  }

  ngOnInit() {
    this.isLoading$ = this.userListFacade.isLoading$;
    this.userRole$ = this.userListFacade.userRole$;
    this.tableConfig$ = this.userListFacade.users$.pipe(
      map((users: AppUser[]) => {
        this.orgOwnerEmail = users.filter(user => user.role === Role.Owner).map(user => user.email)[0];
        return {
          columns: [
            { title: 'user', property: 'username' },
            {
              title: 'role',
              property: 'role',
            },
            { title: 'delete', property: 'delete' },
          ],
          data: users,
        };
      })
    );

    this.availableRoles$ = this.userListFacade.availableRoles$;
    this.availableRolesSubscription = this.userListFacade.availableRoles$.subscribe(value => (this.availableRoles = value));
    this.currentUserId$ = this.userListFacade.currentUserId$;
    this.currentUserEmail$ = this.userListFacade.currentUserEmail$;
    this.messageHeader = this.translate.instant('org_users.add_users_title');
    this.message = this.translate.instant('org_users.add_users_info');
  }

  onChange(user: AppUser): void {
    this.userListFacade.updateUserRole(user.id, user.role);
  }

  onDelete(deletion: UserDeletion): void {
    this.userListFacade.currentUserEmail$
      .pipe(
        take(1),
        switchMap((email: string) =>
          this.dialog
            .open(DeleteDialogComponent, {
              width: '400px',
              data: {
                entityType: this.translate.instant('users.user'),
                entity: deletion.userToDelete,
                options: { name: this.orgOwnerEmail, value: 'owner' },
                isOrganizationOwner: email === this.orgOwnerEmail,
                isSystemUser: true,
              },
            })
            .afterClosed()
        ),
        filter((isClosed: boolean) => isClosed),
        tap(() => this.userListFacade.deleteUser(deletion))
      )
      .subscribe();
  }

  ngOnDestroy(): void {
    this.availableRolesSubscription.unsubscribe();
  }
}
