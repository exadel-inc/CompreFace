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
import { MatSnackBar } from '@angular/material';

import { AppSnackBarComponent } from './snackbar.component';

const messageMap = {
  'default-info': 'DEFAULT INFO MESSAGE',
  'default-error': 'DEFAULT ERROR MESSAGE'
};

@Injectable({
  providedIn: 'root'
})
export class SnackBarService {
  constructor(private snackBar: MatSnackBar) { }

  public openInfo(messageCode: string, duration: number = 3000, message?: string): void {
    const data = {
      message: messageCode ? messageMap[messageCode] : message,
      type: 'info'
    };

    this.openSnackBar(data, duration);
  }

  public openError(messageCode: string, duration: number = 8000, message?: string): void {
    const data = {
      message: messageCode ? messageMap[messageCode] : message,
      type: 'error'
    };

    this.openSnackBar(data, duration);
  }

  public openHttpError(message: HttpErrorResponse, duration: number = 8000): void {
    const data = {
      message: message.error.message || message.message,
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
