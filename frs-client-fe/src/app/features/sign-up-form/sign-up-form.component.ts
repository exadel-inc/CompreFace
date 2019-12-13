import { Component, OnInit } from '@angular/core';
import {FormControl, FormGroup, ValidationErrors, ValidatorFn, Validators} from "@angular/forms";
import {User} from "../../data/user";
import {Store} from "@ngrx/store";
import {AppState} from "../../store";
import {SignUp} from "../../store/actions/auth";
import {Observable} from "rxjs";
import {ROUTERS_URL} from "../../data/routers-url.variable";
import {selectAuthState} from "../../store/selectors/auth";

@Component({
  selector: 'app-sign-up-form',
  templateUrl: './sign-up-form.component.html',
  styleUrls: ['./sign-up-form.component.sass']
})
export class SignUpFormComponent implements OnInit {
  form: FormGroup;
  user: User;
  EMAIL_REGEX = '\\S+@\\S+\\.\\S+';
  getState: Observable<any>;
  errorMessage: string | null;
  isLoading = false;
  ROUTERS_URL = ROUTERS_URL;

  passwordMatchValidator: ValidatorFn = (formGroup: FormGroup): ValidationErrors | null => {
    if (formGroup.get('password').value === formGroup.get('confirmPassword').value)
      return null;
    else
      return {passwordMismatch: true};
  };

  constructor(private store: Store<AppState>) {
    this.getState = this.store.select(selectAuthState);
  }

  ngOnInit() {
    this.form = new FormGroup({
      username: new FormControl(),
      email: new FormControl(null, [Validators.required, Validators.pattern(this.EMAIL_REGEX)]),
      password: new FormControl(null, [Validators.required, Validators.minLength(8)]),
      confirmPassword: new FormControl(null, [
        Validators.required,
        Validators.minLength(8),
      ]),
    }, {validators: this.passwordMatchValidator });

    this.getState.subscribe((state) => {
      this.errorMessage = state.errorMessage;
      this.isLoading = state.isLoading;
    });
  }

  onSubmit() {
    this.user = this.form.value;
    const payload = {
      email: this.user.email,
      password: this.user.password,
      username: this.user.username
    };
    this.isLoading = true;
    this.store.dispatch(new SignUp(payload));
  }

}
