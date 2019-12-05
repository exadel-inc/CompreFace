import { Component, OnInit } from '@angular/core';
import {FormControl, FormGroup} from "@angular/forms";
import {User} from "../../../data/user";
import {Store} from "@ngrx/store";
import {AppState} from "../../../store/state/app.state";
import {SignUp} from "../../../store/actions/auth";

@Component({
  selector: 'app-sign-up-form',
  templateUrl: './sign-up-form.component.html',
  styleUrls: ['./sign-up-form.component.sass']
})
export class SignUpFormComponent implements OnInit {
  form: FormGroup;
  user: User;

  constructor(private store: Store<AppState>) {

  }

  ngOnInit() {
    this.form = new FormGroup({
      username: new FormControl(),
      email: new FormControl(),
      password: new FormControl(),
      confirmPassword: new FormControl(),
    })
  }

  onSubmit() {
    console.log(this.form.value);
    this.user = this.form.value;
    const payload = {
      email: this.user.email,
      password: this.user.password,
      username: this.user.username
    };
    this.store.dispatch(new SignUp(payload));
  }

}
