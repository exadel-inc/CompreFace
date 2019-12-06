import {Component, OnInit} from '@angular/core';
import {FormControl, FormGroup} from "@angular/forms";
import {User} from "../../../data/user";
import {Store} from "@ngrx/store";
import {AppState, selectAuthState} from "../../../store/state/app.state";
import {LogIn} from "../../../store/actions/auth";
import {Observable} from "rxjs";

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
    });
  }

  onSubmit() {
    this.user = this.form.value;
    const payload = {
      username: this.user.username,
      password: this.user.password
    };
    this.store.dispatch(new LogIn(payload));
  }

}
