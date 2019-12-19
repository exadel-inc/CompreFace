import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup } from "@angular/forms";
import { User } from "../../data/user";
import { Store } from "@ngrx/store";
import { AppState } from "../../store";
import { Observable } from "rxjs";
import { ROUTERS_URL } from "../../data/routers-url.variable";
import { LogIn } from "../../store/auth/action";
import { selectAuthState } from "../../store/auth/selectors";

@Component({
  selector: 'app-login-form',
  templateUrl: './login-form.component.html',
  styleUrls: ['./login-form.component.sass']
})
export class LoginFormComponent implements OnInit {
  form: FormGroup;
  user: User;
  getState: Observable<any>;
  errorMessage: string | null;
  successMessage: string | null;
  isLoading = false;
  ROUTERS_URL = ROUTERS_URL;

  constructor(private store: Store<AppState>) {
    this.getState = this.store.select(selectAuthState);
  }

  ngOnInit() {
    this.form = new FormGroup({
      username: new FormControl(),
      password: new FormControl(),
    });

    this.getState.subscribe((state) => {
      this.errorMessage = state.errorMessage;
      this.successMessage = state.successMessage;
      this.isLoading = state.isLoading;
    });
  }

  onSubmit() {
    this.user = this.form.value;
    const payload = {
      username: this.user.username,
      password: this.user.password
    };
    this.isLoading = true;
    this.store.dispatch(new LogIn(payload));
  }
}
