/*!
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

import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators, ValidationErrors } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ChangePassword } from 'src/app/data/interfaces/change-password';

const ERROR = 'error';

interface ChangePasswordData {
  changePassword(credentials: ChangePassword): Observable<string>;
}

@Component({
  selector: 'app-change-password-dialog',
  templateUrl: './change-password-dialog.component.html',
  styleUrls: ['./change-password-dialog.component.scss'],
})
export class ChangePasswordDialogComponent implements OnInit, OnDestroy {
  form: FormGroup;
  isOldPasswordIncorrect = false;
  unsubscribe$ = new Subject<void>();

  constructor(
    public dialogRef: MatDialogRef<ChangePasswordDialogComponent>,
    private formBuilder: FormBuilder,
    @Inject(MAT_DIALOG_DATA) public data: ChangePasswordData
  ) {}

  ngOnInit(): void {
    this.form = this.formBuilder.group({
      oldPassword: new FormControl(null, [Validators.required]),
      newPassword: new FormControl(null, [Validators.required, Validators.minLength(8)]),
      confirmPassword: new FormControl(null, [Validators.required, Validators.minLength(8)]),
    });

    this.form
      .get('oldPassword')
      .valueChanges.pipe(takeUntil(this.unsubscribe$))
      .subscribe(() => {
        this.isOldPasswordIncorrect = false;
      });

    this.form.setValidators(this.passwordMatchValidator);
  }

  passwordMatchValidator(formGroup: FormGroup): ValidationErrors | null {
    if (formGroup.get('newPassword').value === formGroup.get('confirmPassword').value) {
      return null;
    } else {
      return { passwordMismatch: true };
    }
  }

  onCancelClick(): void {
    this.dialogRef.close();
  }

  onChangeClick(): void {
    if (this.form.valid) {
      const result: Observable<string> = this.data.changePassword({ ...this.form.value });
      result.pipe(takeUntil(this.unsubscribe$)).subscribe((result: string) => {
        if (result === ERROR) {
          this.isOldPasswordIncorrect = true;
        } else {
          this.dialogRef.close();
        }
      });
    }
  }

  ngOnDestroy(): void {
    this.unsubscribe$.next();
    this.unsubscribe$.complete();
  }
}
