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

@Injectable({
  providedIn: 'root',
})
export class SnackBarService {
  constructor(private snackBar: MatSnackBar, private translate: TranslateService) {}

  openNotification({ messageText, messageOptions, type = 'info' }: { messageText: string; messageOptions?: any; type?: string }): void {
    const message = this.translate.instant(messageText, messageOptions);
    const duration = type === 'info' ? 3000 : 8000;
    const data = {
      message,
      type,
    };

    this.openSnackBar(data, duration);
  }

  openHttpError(message: HttpErrorResponse, duration: number = 8000): void {
    const errorMessage =
      !(message.error instanceof ProgressEvent) && message.error
        ? message.error.error_description
          ? message.error.error_description
          : message.error
        : this.translate.instant('common.unknown_error');
    const data = {
      message: errorMessage.message ? errorMessage.message : errorMessage,
      type: 'error',
    };

    if (message.status !== 502 && message.status !== 504) {
      this.openSnackBar(data, duration);
    }

    if (message.status === 502) {
      console.error(data.message);
    }
  }

  private openSnackBar(data, duration): void {
    this.snackBar.openFromComponent(AppSnackBarComponent, {
      duration,
      data,
      verticalPosition: 'top',
      panelClass: [data.type],
    });
  }
}
