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
import { ChangeDetectionStrategy, Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { combineLatest, Observable } from 'rxjs';
import { map, startWith } from 'rxjs/operators';
import { EMAIL_REGEXP_PATTERN } from 'src/app/core/constants';

@Component({
  selector: 'app-invite-dialog',
  templateUrl: './invite-dialog.component.html',
  styleUrls: ['./invite-dialog.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InviteDialogComponent implements OnInit {
  availableRoles: string[];
  form: FormGroup;
  users: string[];
  filteredOptions$: Observable<string[]>;
  actionType: string;

  constructor(
    private formBuilder: FormBuilder,
    public dialogRef: MatDialogRef<InviteDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.availableRoles = this.data.availableRoles;
    this.actionType = this.data.actionType || 'invite';
  }

  ngOnInit() {
    this.form = this.formBuilder.group({
      userEmail: new FormControl(null, [Validators.required, Validators.pattern(EMAIL_REGEXP_PATTERN)]),
      role: new FormControl(this.availableRoles[0], [Validators.required]),
    });

    if (this.data.options$) {
      this.filteredOptions$ = combineLatest([this.data.options$, this.form.controls.userEmail.valueChanges.pipe(startWith(''))]).pipe(
        map(([options, value]) => this.filter(options as string[], value))
      );
    }
  }

  onInviteClick(): void {
    if (this.form.valid) {
      this.dialogRef.close({
        ...this.form.value,
      });
    }
  }

  private filter(options: string[], value: string): string[] {
    const filterValue = value ? value.toLowerCase() : '';
    return options ? options.filter(option => option && option.toLowerCase().includes(filterValue)) : [''];
  }
}
