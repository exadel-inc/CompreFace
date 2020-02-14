import {Component, OnDestroy, OnInit} from '@angular/core';
import {FormControl, FormGroup, ValidationErrors, ValidatorFn, Validators} from '@angular/forms';
import {User} from '../../data/user';
import {Store} from '@ngrx/store';
import {AppState} from '../../store';
import {Observable, Subscription} from 'rxjs';
import {ROUTERS_URL} from '../../data/routers-url.variable';
import {signUp} from '../../store/auth/action';
import {selectAuthState} from '../../store/auth/selectors';
import {EMAIL_REGEXP_PATTERN} from 'src/app/core/constants';
import {SignUp} from '../../data/sign-up';

@Component({
  selector: 'app-sign-up-form',
  templateUrl: './sign-up-form.component.html',
  styleUrls: ['./sign-up-form.component.sass']
})
export class SignUpFormComponent implements OnInit, OnDestroy {
  form: FormGroup;
  user: User;
  getState: Observable<any>;
  errorMessage: string | null;
  isLoading = false;
  ROUTERS_URL = ROUTERS_URL;
  stateSubscription: Subscription;

  passwordMatchValidator: ValidatorFn = (formGroup: FormGroup): ValidationErrors | null => {
    if (formGroup.get('password').value === formGroup.get('confirmPassword').value) {
      return null;
    } else {
      return { passwordMismatch: true };
    }
  };

  constructor(private store: Store<AppState>) {
    this.getState = this.store.select(selectAuthState);
  }

  ngOnInit() {
    this.form = new FormGroup({
      firstName: new FormControl(),
      lastName: new FormControl(),
      email: new FormControl(null, [Validators.required, Validators.pattern(EMAIL_REGEXP_PATTERN)]),
      password: new FormControl(null, [Validators.required, Validators.minLength(8)]),
      confirmPassword: new FormControl(null, [
        Validators.required,
        Validators.minLength(8),
      ]),
    }, { validators: this.passwordMatchValidator });

    this.stateSubscription = this.getState.subscribe((state) => {
      this.errorMessage = state.errorMessage;
      this.isLoading = state.isLoading;
    });
  }

  ngOnDestroy() {
    this.stateSubscription.unsubscribe();
  }

  onSubmit() {
    this.user = this.form.value;
    const payload: SignUp = {
      email: this.user.email,
      password: this.user.password,
      firstName: this.user.firstName,
      lastName: this.user.lastName
    };
    this.isLoading = true;
    this.store.dispatch(signUp(payload));
  }
}
