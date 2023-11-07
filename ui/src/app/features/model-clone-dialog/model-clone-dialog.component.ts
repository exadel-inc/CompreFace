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
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MAX_INPUT_LENGTH } from 'src/app/core/constants';

@Component({
  selector: 'app-model-clone-dialog',
  templateUrl: './model-clone-dialog.component.html',
  styleUrls: ['./model-clone-dialog.component.scss'],
})
export class ModelCloneDialogComponent {
  initialName: string;
  alreadyExists: boolean;
  maxInputLength: number = MAX_INPUT_LENGTH;

  constructor(public dialogRef: MatDialogRef<ModelCloneDialogComponent>, @Inject(MAT_DIALOG_DATA) public data: any) {}

  onChange(name): void {
    this.alreadyExists = !!this.data.models.find(model => model.name === name);
  }

  get isCloneDisabled(): boolean {
    return this.initialName ? this.initialName.length < 1 : true;
  }
}
