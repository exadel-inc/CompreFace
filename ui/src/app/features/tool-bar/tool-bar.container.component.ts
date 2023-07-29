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
import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';

import { ToolBarFacade } from './tool-bar.facade';
import { ChangePassword } from '../../data/interfaces/change-password';
import { EditUserInfo } from '../../data/interfaces/edit-user-info';
import { map, take } from 'rxjs/operators';
import { CircleLoadingProgressEnum } from 'src/app/data/enums/circle-loading-progress.enum';
import { TranslateService } from '@ngx-translate/core';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-tool-bar-container',
  template: ` <app-tool-bar
    [userAvatarInfo]="userAvatarInfo$ | async"
    [userFirstName]="userFirstName$ | async"
    [userLastName]="userLastName$ | async"
    [isUserInfoAvailable]="isUserInfoAvailable$ | async"
    [itemsInProgress]="itemsInProgress$ | async"
    (logout)="logout()"
    (signUp)="goSignUp()"
    (changePassword)="changePassword($event)"
    (editUserInfo)="editUserInfo($event)"
  >
  </app-tool-bar>`,
})
export class ToolBarContainerComponent implements OnInit {
  userAvatarInfo$: Observable<string>;
  userFirstName$: Observable<string>;
  userLastName$: Observable<string>;
  isUserInfoAvailable$: Observable<boolean>;
  itemsInProgress$: Observable<boolean>;

  constructor(private toolBarFacade: ToolBarFacade, private translate: TranslateService, private dialog: MatDialog) {
    this.itemsInProgress$ = this.toolBarFacade.collectionItems$.pipe(
      map(collection => !!collection.find(item => item.status === CircleLoadingProgressEnum.InProgress))
    );
  }

  ngOnInit() {
    this.userAvatarInfo$ = this.toolBarFacade.userAvatarInfo$;
    this.userFirstName$ = this.toolBarFacade.userFirstName$;
    this.userLastName$ = this.toolBarFacade.userLastName$;
    this.isUserInfoAvailable$ = this.toolBarFacade.isUserInfoAvailable$.pipe(map(id => !!id));
  }

  goSignUp() {
    this.toolBarFacade.goSignUp();
  }

  logout() {
    this.itemsInProgress$.pipe(take(1)).subscribe(loading => {
      if (loading) {
        this.openDialog();
      } else {
        this.toolBarFacade.logout();
      }
    });
  }

  changePassword(payload: ChangePassword) {
    this.toolBarFacade.changePassword(payload);
  }

  editUserInfo(payload: EditUserInfo) {
    this.toolBarFacade.editUserInfo(payload);
  }

  openDialog(): void {
    const dialog = this.dialog.open(ConfirmDialogComponent, {
      panelClass: 'custom-mat-dialog',
      data: {
        title: this.translate.instant('org_users.confirm_dialog.title'),
        description: this.translate.instant('org_users.confirm_dialog.confirmation_question'),
      },
    });

    dialog.afterClosed().subscribe(confirm => {
      if (!confirm) return;

      this.toolBarFacade.logout();
    });
  }
}
