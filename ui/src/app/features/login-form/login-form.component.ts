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
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Store } from '@ngrx/store';
import { Observable, Subscription } from 'rxjs';
import { EMAIL_REGEXP_PATTERN } from 'src/app/core/constants';

import { environment } from '../../../environments/environment';
import { Routes } from '../../data/enums/routers-url.enum';
import { User } from '../../data/interfaces/user';
import { AppState } from '../../store';
import { logIn, resetErrorMessage } from '../../store/auth/action';
import { selectAuthState } from '../../store/auth/selectors';

@Component({
  selector: 'app-login-form',
  templateUrl: './login-form.component.html',
  styleUrls: ['./login-form.component.scss'],
})
export class LoginFormComponent implements OnInit, OnDestroy {
  form: FormGroup;
  user: User;
  getState: Observable<any>;
  isLoading = false;
  routes = Routes;
  stateSubscription: Subscription;
  env = environment;

  constructor(private store: Store<AppState>) {
    this.getState = this.store.select(selectAuthState);
  }

  ngOnInit() {
    this.form = new FormGroup({
      email: new FormControl(null, [Validators.required, Validators.pattern(EMAIL_REGEXP_PATTERN)]),
      password: new FormControl(null, [Validators.required]),
    });

    this.stateSubscription = this.getState.subscribe(state => {
      this.isLoading = state.isLoading;
    });
  }

  ngOnDestroy() {
    this.store.dispatch(resetErrorMessage());
    this.stateSubscription.unsubscribe();
  }

  onSubmit() {
    this.user = this.form.value;
    const payload = {
      email: this.user.email.trim(),
      password: this.user.password.trim(),
    };
    this.isLoading = true;
    this.store.dispatch(logIn(payload));
  }
}
