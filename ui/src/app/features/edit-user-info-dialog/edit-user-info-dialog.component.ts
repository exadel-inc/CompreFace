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

import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'app-edit-user-info-dialog',
  templateUrl: './edit-user-info-dialog.component.html',
})
export class EditUserInfoDialogComponent implements OnInit {
  form: FormGroup;
  firstName: string;
  lastName: string;

  constructor(
    public dialogRef: MatDialogRef<EditUserInfoDialogComponent>,
    private formBuilder: FormBuilder,
    @Inject(MAT_DIALOG_DATA) public data: { userName: string }
  ) {}

  ngOnInit(): void {
    [this.firstName, this.lastName] = this.data?.userName.split(' ');
    this.form = this.formBuilder.group({
      firstName: new FormControl(this.firstName, [Validators.required]),
      lastName: new FormControl(this.lastName, [Validators.required]),
    });
  }

  onCancelClick(): void {
    this.dialogRef.close();
  }

  onChangeClick(): void {
    if (this.form.valid) {
      this.dialogRef.close({
        ...this.form.value,
      });
    }
  }
}
