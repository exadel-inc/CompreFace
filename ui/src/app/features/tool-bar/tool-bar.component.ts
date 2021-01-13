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
import { TranslateService } from '@ngx-translate/core';
import { first } from 'rxjs/operators';

import { ChangePasswordDialogComponent } from '../change-password-dialog/change-password-dialog.component';

@Component({
  selector: 'app-tool-bar',
  templateUrl: './tool-bar.component.html',
  styleUrls: ['./tool-bar.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ToolBarComponent {
  @Input() userAvatarInfo: string;
  @Input() userName: string;
  @Input() isUserInfoAvailable: boolean;
  @Output() logout = new EventEmitter();
  @Output() signUp = new EventEmitter();
  @Output() changePassword = new EventEmitter();

  constructor(private dialog: MatDialog, private translate: TranslateService) {}

  goSignUp() {
    this.signUp.emit();
  }

  doLogout() {
    this.logout.emit();
  }

  onChangePassword() {
    const dialog = this.dialog.open(ChangePasswordDialogComponent, {
      width: '350px',
      data: {
        entityType: this.translate.instant('applications.header.title'),
      },
    });

    dialog
      .afterClosed()
      .pipe(first())
      .subscribe(result => {
        if (result) {
          this.changePassword.emit(result);
        }
      });
  }
}
