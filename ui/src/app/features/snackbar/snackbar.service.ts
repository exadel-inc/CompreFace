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
import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TranslateService } from '@ngx-translate/core';

import { AppSnackBarComponent } from './snackbar.component';

const messageMap = {
  'default-info': 'DEFAULT INFO MESSAGE',
  'default-error': 'DEFAULT ERROR MESSAGE'
};

@Injectable({
  providedIn: 'root'
})
export class SnackBarService {
  constructor(
    private snackBar: MatSnackBar,
    private translate: TranslateService
  ) { }

  openInfo(messageParam: string, duration: number = 3000): void {
    const message = this.translate.instant(messageParam);
    const data = {
      message: message ? message : messageMap[message],
      type: 'info'
    };

    this.openSnackBar(data, duration);
  }

  openError(messageParam: string, duration: number = 8000): void {
    const message = this.translate.instant(messageParam);
    const data = {
      message: message ? message : messageMap[message],
      type: 'error'
    };

    this.openSnackBar(data, duration);
  }

  openWarning(messageParam: string, duration: number = 8000): void {
    const message = this.translate.instant(messageParam);
    const data = {
      message: message ? message : messageMap[message],
      type: 'warning'
    };

    this.openSnackBar(data, duration);
  }

  openHttpError(message: HttpErrorResponse, duration: number = 8000): void {
    const errorMessage = message.error || message;
    const data = {
      message: errorMessage.message ? errorMessage.message : errorMessage,
      type: 'error'
    };

    this.openSnackBar(data, duration);
  }

  private openSnackBar(data, duration): void {
    this.snackBar.openFromComponent(AppSnackBarComponent, {
      duration,
      data,
      verticalPosition: 'top',
      panelClass: ['app-snackbar-panel', data.type]
    });
  }
}
