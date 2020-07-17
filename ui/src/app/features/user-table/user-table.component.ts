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

import {ChangeDetectionStrategy, Component, EventEmitter, Input, OnChanges, Output, SimpleChanges} from '@angular/core';
import {AppUser} from 'src/app/data/appUser';


import {FormArray, FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {MatTableDataSource} from '@angular/material';


@Component({
  selector: 'app-user-table',
  templateUrl:  './user-table.component.html',
  styleUrls: ['./user-table.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UserTableComponent implements OnChanges {
  form: FormGroup;
  dataSource: MatTableDataSource<any>;
  displayedColumns = ['name', 'role', 'delete'];

  @Input() users: AppUser[];
  @Input() availableRoles: string[];
  @Input() currentUserId: string;
  @Input() userRole: string;
  @Input() currentUserEmail: string;
  @Input() createMessage: string;
  @Input() createHeader: string;
  @Input() isLoading: boolean;
  @Output() deleteUser = new EventEmitter<any>();
  @Output() changeRole = new EventEmitter<any>();

  constructor(
    private formBuilder: FormBuilder
  ) {
    this.form = this.formBuilder.group({
      users: this.formBuilder.array([])
    });
  }

  ngOnChanges(simpleChanges: SimpleChanges) {
    if(simpleChanges.users && simpleChanges.users.currentValue) {
      this.form.setControl('users', this.usersAsFormArray());
      this.dataSource = new MatTableDataSource((this.form.get('users') as FormArray).controls);
      this.dataSource.filterPredicate = (data: FormGroup, filter: string) => {
        return Object.values(data.controls).some(x => x.value === filter);
      };
    }
  }

  get usersForm(): FormArray {
    return this.form.get('users') as FormArray;
  }

  usersAsFormArray(): FormArray {
    const fg$ = this.users.map(user => this.userAsFormGroup(user));
    return new FormArray(fg$);
  }

  userAsFormGroup(appUser: AppUser): FormGroup {
    return new FormGroup({
      id: new FormControl(appUser.id),
      userId: new FormControl(appUser.userId),
      firstName: new FormControl(appUser.firstName, Validators.required),
      lastName: new FormControl(appUser.lastName, Validators.required),
      role: new FormControl(appUser.role, Validators.required),
      email: new FormControl(appUser.email),
      ownerOfApplications: new FormControl(appUser.ownerOfApplications)
    });
  }

  isRoleChangeAllowed(userRole: string): boolean {
    return this.userRole !== 'USER' && this.availableRoles.indexOf(userRole) > -1;
  }

  onChangeRole(event, userGroup: FormGroup) {
    const user = {
      ...userGroup.value,
      role: event,
    };
    this.changeRole.emit(user);
  }

  onDelete(event, userGroup: FormGroup) {
    const user = {
      ...userGroup.value,
      role: event,
    };
    this.deleteUser.emit(user);
  }
}
