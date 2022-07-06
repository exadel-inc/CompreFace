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
    this.logout.emit();
  }

  onChangePassword() {
    const dialog = this.dialog.open(ChangePasswordDialogComponent, {
      panelClass: 'custom-mat-dialog',
      data: {
        entityType: this.translate.instant('applications.header.title'),
      },
    });

    dialog
      .afterClosed()
      .pipe(
        first(),
        filter(result => result)
      )
      .subscribe(result => this.changePassword.emit(result));
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
    this.itemsInProgress ? this.openDialog(path, id) : this.router.navigate([path], { queryParams: { app: id } });
  }

  openDialog(path: string, id?: string): void {
    const dialog = this.dialog.open(ConfirmDialogComponent, {
      panelClass: 'custom-mat-dialog',
    });

    dialog.afterClosed().subscribe(confirm => {
      if (!confirm) return;
      this.router.navigate([path], { queryParams: { app: id } });
    });
  }
}
