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
import { UserRole } from '../../data/interfaces/user-role';

@Component({
  selector: 'app-create-dialog',
  templateUrl: './role-edit-dialog.component.html',
  styleUrls: ['./role-edit-dialog.component.scss'],
})
export class RoleEditDialogComponent {
  constructor(public dialogRef: MatDialogRef<RoleEditDialogComponent>, @Inject(MAT_DIALOG_DATA) public data: any) {}

  role: UserRole = { id: null, role: null };

  setRole(event, element): void {
    this.role = { id: element.id, role: event.value };
  }

  onCancelClick(): void {
    this.dialogRef.close();
  }
}
