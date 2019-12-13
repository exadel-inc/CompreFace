import { Component, OnInit } from '@angular/core';
import {AppState} from "../../store";
import {Store} from "@ngrx/store";
import {LogOut} from "../../store/actions/auth";
import {Observable} from "rxjs";
import {selectAuthState} from "../../store/selectors/auth";

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
      this.user = state.user;
    });
  }

  logout() {
    console.log('logout');
    this.store.dispatch(new LogOut);
  }

}
