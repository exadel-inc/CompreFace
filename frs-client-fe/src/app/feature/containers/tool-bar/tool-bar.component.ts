import { Component, OnInit } from '@angular/core';
import {AppState, selectAuthState} from "../../../store/state/app.state";
import {Store} from "@ngrx/store";
import {LogOut} from "../../../store/actions/auth";
import {Observable} from "rxjs";

@Component({
  selector: 'app-tool-bar',
  templateUrl: './tool-bar.component.html',
  styleUrls: ['./tool-bar.component.sass']
})
export class ToolBarComponent implements OnInit {
  getState: Observable<any>;
  isAuthenticated: false;
  user = null;

  constructor( private store: Store<AppState>) {
    this.getState = this.store.select(selectAuthState);
  }

  ngOnInit() {
    this.getState.subscribe((state) => {
      this.isAuthenticated = state.isAuthenticated;
      this.user.name = state.user.name;
    });
  }

  logout() {
    console.log('logout');
    this.store.dispatch(new LogOut);
  }

}
