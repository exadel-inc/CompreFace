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

import { ChangeDetectionStrategy } from '@angular/core';
import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MAX_INPUT_LENGTH } from 'src/app/core/constants';
import { CreateDialogComponent } from '../create-dialog/create-dialog.component';

@Component({
  selector: 'app-edit-dialog',
  templateUrl: './edit-dialog.component.html',
  styleUrls: ['./edit-dialog.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EditDialogComponent extends CreateDialogComponent {
  initialName: string;
  panelOpenState: boolean = false;
  deleteInput: string = '';
  alreadyExists: boolean;
  maxInputLength: number = MAX_INPUT_LENGTH;

  get isRenameDisabled(): any {
    return this.data.entityName === this.initialName || !this.data.entityName;
  }

  constructor(public dialogRef: MatDialogRef<EditDialogComponent>, @Inject(MAT_DIALOG_DATA) public data: any) {
    super(dialogRef, data);
    this.initialName = data.entityName;
  }

  onChange(name: string): void {
    this.alreadyExists = !!this.data.models.find(model => model.name === name);
    this.checkForForbiddenChars(name);
  }

  onSave() {
    this.dialogRef.close({
      update: true,
      name: this.initialName,
    });
  }

  onDelete() {
    this.dialogRef.close({ update: false });
  }
}
