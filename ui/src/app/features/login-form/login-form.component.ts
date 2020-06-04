import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Store } from '@ngrx/store';
import { Observable, Subscription } from 'rxjs';
import { EMAIL_REGEXP_PATTERN } from 'src/app/core/constants';

import { ROUTERS_URL } from '../../data/routers-url.variable';
import { User } from '../../data/user';
import { AppState } from '../../store';
import { logIn, resetErrorMessage } from '../../store/auth/action';
import { selectAuthState } from '../../store/auth/selectors';

@Component({
  selector: 'app-login-form',
  templateUrl: './login-form.component.html',
  styleUrls: ['./login-form.component.scss']
})
export class LoginFormComponent implements OnInit, OnDestroy {
  form: FormGroup;
  user: User;
  getState: Observable<any>;
  isLoading = false;
  ROUTERS_URL = ROUTERS_URL;
  stateSubscription: Subscription;

  constructor(private store: Store<AppState>) {
    this.getState = this.store.select(selectAuthState);
  }

  ngOnInit() {
    this.form = new FormGroup({
      email: new FormControl(null, [Validators.required, Validators.pattern(EMAIL_REGEXP_PATTERN)]),
      password: new FormControl(null, [Validators.required]),
    });

    this.stateSubscription = this.getState.subscribe((state) => {
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
      email: this.user.email,
      password: this.user.password
    };
    this.isLoading = true;
    this.store.dispatch(logIn(payload));
  }
}
