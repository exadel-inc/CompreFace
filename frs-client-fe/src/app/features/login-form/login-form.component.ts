import {Component, OnDestroy, OnInit} from '@angular/core';
import {FormControl, FormGroup} from '@angular/forms';
import {User} from '../../data/user';
import {Store} from '@ngrx/store';
import {AppState} from '../../store';
import {Observable, Subscription} from 'rxjs';
import {ROUTERS_URL} from '../../data/routers-url.variable';
import {logIn, resetErrorMessage} from '../../store/auth/action';
import {selectAuthState} from '../../store/auth/selectors';

@Component({
  selector: 'app-login-form',
  templateUrl: './login-form.component.html',
  styleUrls: ['./login-form.component.scss']
})
export class LoginFormComponent implements OnInit, OnDestroy {
  form: FormGroup;
  user: User;
  getState: Observable<any>;
  errorMessage: string | null;
  successMessage: string | null;
  isLoading = false;
  ROUTERS_URL = ROUTERS_URL;
  stateSubscription: Subscription;

  constructor(private store: Store<AppState>) {
    this.getState = this.store.select(selectAuthState);
  }

  ngOnInit() {
    this.form = new FormGroup({
      email: new FormControl(),
      password: new FormControl(),
    });

    this.stateSubscription = this.getState.subscribe((state) => {
      this.errorMessage = state.errorMessage;
      this.successMessage = state.successMessage;
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
