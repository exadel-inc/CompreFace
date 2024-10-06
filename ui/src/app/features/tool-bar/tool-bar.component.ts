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
import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { filter, first } from 'rxjs/operators';

import { ChangePasswordDialogComponent } from '../change-password-dialog/change-password-dialog.component';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog.component';
import { EditUserInfoDialogComponent } from '../edit-user-info-dialog/edit-user-info-dialog.component';
import { Observable } from 'rxjs';
import { ChangePassword } from 'src/app/data/interfaces/change-password';

@Component({
  selector: 'app-tool-bar',
  templateUrl: './tool-bar.component.html',
  styleUrls: ['./tool-bar.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ToolBarComponent {
  @Input() userAvatarInfo: string;
  @Input() userFirstName: string;
  @Input() userLastName: string;
  @Input() isUserInfoAvailable: boolean;
  @Input() itemsInProgress: boolean;
  @Input() passwordChangeResult: Observable<string>;
  @Output() logout = new EventEmitter();
  @Output() signUp = new EventEmitter();
  @Output() changePassword = new EventEmitter();
  @Output() editUserInfo = new EventEmitter();

  openMenu = false;

  constructor(private dialog: MatDialog, private translate: TranslateService, private router: Router) {}

  changeArrowIcon(): void {
    this.openMenu = !this.openMenu;
  }

  goSignUp() {
    this.signUp.emit();
  }

  doLogout() {
    if (this.itemsInProgress) {
      this.openDialog(true);
    } else {
      this.logout.emit();
    }
  }

  onChangePassword() {
    this.dialog.open(ChangePasswordDialogComponent, {
      panelClass: 'custom-mat-dialog',
      data: {
        entityType: this.translate.instant('applications.header.title'),
        changePassword: (result: ChangePassword) => {
          this.changePassword.emit(result);
          return this.passwordChangeResult;
        },
      },
    });
  }

  onEditUserInfo() {
    const dialog = this.dialog.open(EditUserInfoDialogComponent, {
      panelClass: 'custom-mat-dialog',
      data: { firstName: this.userFirstName, lastName: this.userLastName },
    });

    dialog
      .afterClosed()
      .pipe(
        first(),
        filter(result => result)
      )
      .subscribe(result => this.editUserInfo.emit(result));
  }

  onNavigate(path: string, id?: string) {
    this.itemsInProgress ? this.openDialog(false, path, id) : this.router.navigate([path], { queryParams: { app: id } });
  }

  openDialog(isLogout: boolean, path?: string, id?: string): void {
    const dialog = this.dialog.open(ConfirmDialogComponent, {
      panelClass: 'custom-mat-dialog',
      data: {
        title: this.translate.instant('org_users.confirm_dialog.title'),
        description: this.translate.instant('org_users.confirm_dialog.confirmation_question'),
      },
    });

    dialog.afterClosed().subscribe(confirm => {
      if (!confirm) return;
      if (isLogout) {
        this.logout.emit();
      } else {
        this.router.navigate([path], { queryParams: { app: id } });
      }
    });
  }
}
