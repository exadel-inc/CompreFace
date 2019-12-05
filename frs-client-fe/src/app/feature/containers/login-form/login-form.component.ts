import {Component, OnInit} from '@angular/core';
import {FormControl, FormGroup} from "@angular/forms";
import {User} from "../../../data/user";
import {Store} from "@ngrx/store";
import {AppState} from "../../../store/state/app.state";
import {LogIn} from "../../../store/actions/auth";

@Component({
  selector: 'app-login-form',
  templateUrl: './login-form.component.html',
  styleUrls: ['./login-form.component.sass']
})
export class LoginFormComponent implements OnInit {
  form: FormGroup;
  user: User;

  constructor(private store: Store<AppState>) {

  }

  ngOnInit() {
    this.form = new FormGroup({
      username: new FormControl(),
      password: new FormControl(),
    })
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
