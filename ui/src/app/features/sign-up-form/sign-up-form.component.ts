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
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormControl, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { Store } from '@ngrx/store';
import { Observable, Subscription } from 'rxjs';
import { EMAIL_REGEXP_PATTERN, MAX_INPUT_LENGTH } from 'src/app/core/constants';

import { Routes } from '../../data/enums/routers-url.enum';
import { SignUp } from '../../data/interfaces/sign-up';
import { User } from '../../data/interfaces/user';
import { AppState } from '../../store';
import { resetErrorMessage, signUp } from '../../store/auth/action';
import { selectLoadingState } from '../../store/auth/selectors';
import { selectDemoPageAvailability } from '../../store/demo/selectors';
import { loadDemoStatus } from '../../store/demo/action';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-sign-up-form',
  templateUrl: './sign-up-form.component.html',
  styleUrls: ['./sign-up-form.component.scss'],
})
export class SignUpFormComponent implements OnInit, OnDestroy {
  form: FormGroup;
  user: User;
  isLoading$: Observable<boolean>;
  isDemoPageAvailable$: Observable<boolean>;
  isLoading = false;
  routes = Routes;
  stateSubscription: Subscription;
  isFirstRegistration: boolean;
  env = environment;
  maxInputLength = MAX_INPUT_LENGTH;

  passwordMatchValidator: ValidatorFn = (formGroup: FormGroup): ValidationErrors | null => {
    if (formGroup.get('password').value === formGroup.get('confirmPassword').value) {
      return null;
    } else {
      return { passwordMismatch: true };
    }
  };

  constructor(private store: Store<AppState>) {
    this.isLoading$ = this.store.select(selectLoadingState);
    this.isDemoPageAvailable$ = this.store.select(selectDemoPageAvailability);
  }

  ngOnInit() {
    this.form = new FormGroup(
      {
        firstName: new FormControl(this.user?.firstName, Validators.maxLength(this.maxInputLength)),
        lastName: new FormControl(this.user?.lastName, Validators.maxLength(this.maxInputLength)),
        email: new FormControl(null, [Validators.required, Validators.pattern(EMAIL_REGEXP_PATTERN)]),
        password: new FormControl(null, [Validators.required, Validators.minLength(8)]),
        confirmPassword: new FormControl(null, [Validators.required, Validators.minLength(8)]),
        isAllowStatistics: new FormControl(true),
      },
      { validators: this.passwordMatchValidator }
    );

    this.store.dispatch(loadDemoStatus());
    this.stateSubscription = this.isDemoPageAvailable$.subscribe(isDemoPageAvailable => {
      this.isFirstRegistration = isDemoPageAvailable;
    });
  }

  ngOnDestroy() {
    this.store.dispatch(resetErrorMessage());
    this.stateSubscription.unsubscribe();
  }

  onSubmit() {
    this.user = this.form.value;
    const payload: SignUp = {
      email: this.user.email.trim(),
      password: this.user.password.trim(),
      firstName: this.user.firstName.trim(),
      lastName: this.user.lastName.trim(),
    };
    if (this.isFirstRegistration) {
      payload.isAllowStatistics = this.user.isAllowStatistics;
    }
    this.store.dispatch(signUp(payload));
  }
}
