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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {AppUser} from 'src/app/data/appUser';


import {FormArray, FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {MatTableDataSource} from '@angular/material';


@Component({
  selector: 'app-user-table',
  templateUrl:  './user-table.component.html',
  styleUrls: ['./user-table.component.scss']
})
export class UserTableComponent implements OnInit {
  form: FormGroup;
  dataSource: MatTableDataSource<any>;
  displayedColumns = ['name', 'role', 'delete'];

  @Input() users$: Observable<AppUser[]>;
  @Input() availableRoles: string[];
  @Input() availableRoles$: Observable<string[]>;
  @Input() currentUserId: string;
  @Input() userRole: string;
  @Input() currentUserEmail: string;
  @Input() createMessage: string;
  @Input() isLoading: boolean;
  @Output() deleteUser = new EventEmitter<any>();
  @Output() changeRole = new EventEmitter<any>();

  constructor(
    private formBuilder: FormBuilder
  ) {
  }

  ngOnInit() {
    this.form = this.formBuilder.group({
      users: this.formBuilder.array([])
    });
    this.usersAsFormArray().subscribe(users => {
      this.form.setControl('users', users);
      this.dataSource = new MatTableDataSource((this.form.get('users') as FormArray).controls);
      this.dataSource.filterPredicate = (data: FormGroup, filter: string) => {
        return Object.values(data.controls).some(x => x.value === filter);
      };
    });
  }

  get users(): FormArray {
    return this.form.get('users') as FormArray;
  }

  usersAsFormArray(): Observable<FormArray> {
    return this.users$.pipe(map((appUsers: AppUser[]) => {
      const fg$ = appUsers.map(user => this.userAsFormGroup(user));
      return new FormArray(fg$);
    }));
  }

  userAsFormGroup(appUser: AppUser): FormGroup {
    return new FormGroup({
      id: new FormControl(appUser.id),
      name: new FormControl(appUser.firstName, Validators.required),
      lastName: new FormControl(appUser.lastName, Validators.required),
      role: new FormControl(appUser.role, Validators.required),
      email: new FormControl(appUser.email),
      ownerOfApplications: new FormControl(appUser.ownerOfApplications)
    });
  }

  isRoleChangeAllowed(userRole: string): Observable<boolean> {
    return this.userRole !== 'USER' && this.availableRoles$.pipe(map(availableRoles => availableRoles.indexOf(userRole) > -1));
  }

  onChangeRole(event, userGroup: FormGroup) {
    const user = {
      firstName: userGroup.get('name').value,
      lastName: userGroup.get('lastName').value,
      role: event,
      id: userGroup.get('id').value,
    };
    this.changeRole.emit(user);
  }

  onDelete(event, userGroup: FormGroup) {
    const user = {
      firstName: userGroup.get('name').value,
      lastName: userGroup.get('lastName').value,
      userId: userGroup.get('id').value,
      email: userGroup.get('email').value,
      ownerOfApplications: userGroup.get('ownerOfApplications').value
    };
    this.deleteUser.emit(user);
  }
}
