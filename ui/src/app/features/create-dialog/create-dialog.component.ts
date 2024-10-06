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
import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MAX_INPUT_LENGTH } from 'src/app/core/constants';

@Component({
  selector: 'app-create-dialog',
  templateUrl: './create-dialog.component.html',
  styleUrls: ['./create-dialog.component.scss'],
})
export class CreateDialogComponent {
  alreadyExists: boolean;
  maxInputLength: number = MAX_INPUT_LENGTH;
  isForbiddenChar: boolean;

  constructor(public dialogRef: MatDialogRef<CreateDialogComponent>, @Inject(MAT_DIALOG_DATA) public data: any) {}

  onChange(name: string): void {
    this.alreadyExists = !!this.data.nameList.find(appName => appName === name);
    this.checkForForbiddenChars(name);
  }

  checkForForbiddenChars(name: string): void {
    const forbidenChars = /[;\/\\]/;
    this.isForbiddenChar = forbidenChars.test(name);
  }
}
