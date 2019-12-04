import { Component, OnInit } from '@angular/core';
import {Store} from "@ngrx/store";
import {User} from "../../data/user";
import {AppState} from "../../store/state/app.state";
import {LogIn} from "../../store/actions/auth";

@Component({
  selector: 'app-sign-up',
  templateUrl: './sign-up.component.html',
  styleUrls: ['./sign-up.component.sass']
})
export class SignUpComponent implements OnInit {

  user: User;

  constructor(
    private store: Store<AppState>
  ) { }

  ngOnInit() {
  }

  onSubmit(): void {
    const payload = {
      email: this.user.email,
      password: this.user.password
    };
    this.store.dispatch(new LogIn(payload));
  }
}
