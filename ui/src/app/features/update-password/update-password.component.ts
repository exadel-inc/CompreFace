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

import { Component, ViewChild } from '@angular/core';
import { FormControl, FormGroup, NgForm, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { EMAIL_REGEXP_PATTERN } from 'src/app/core/constants';
import { Routes } from 'src/app/data/enums/routers-url.enum';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-update-password',
  templateUrl: './update-password.component.html',
  styleUrls: ['./update-password.component.scss'],
})
export class UpdatePasswordComponent {
  form: FormGroup;
  password: string;
  repeatPassword: string;
  env = environment;
  routes = Routes;

  ngOnInit() {
    this.form = new FormGroup(
      {
        password: new FormControl(null, [Validators.required, Validators.minLength(8)]),
        confirmPassword: new FormControl(null, [Validators.required, Validators.minLength(8)]),
      },
      { validators: this.passwordMatchValidator }
    );

    console.log(this.form);
  }

  passwordMatchValidator: ValidatorFn = (formGroup: FormGroup): ValidationErrors | null => {
    if (formGroup.get('password').value === formGroup.get('confirmPassword').value) {
      return null;
    } else {
      return { passwordMismatch: true };
    }
  };

  onSubmit() {}
}
