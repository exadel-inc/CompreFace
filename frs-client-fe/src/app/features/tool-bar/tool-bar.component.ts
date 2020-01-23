import { Component, OnInit } from '@angular/core';
import {AppState} from "../../store";
import {Store} from "@ngrx/store";
import {Observable} from "rxjs";
import {LogOut} from "../../store/auth/action";
import {selectAuthState} from "../../store/auth/selectors";
import { Router } from '@angular/router';

@Component({
  selector: 'app-tool-bar',
  templateUrl: './tool-bar.component.html',
  styleUrls: ['./tool-bar.component.sass']
})
export class ToolBarComponent implements OnInit {
  getState: Observable<any>;
  isAuthenticated: false;
  user = null;
  currentUrl: string;

  constructor( private store: Store<AppState>, private router: Router) {
    this.getState = this.store.select(selectAuthState);
  }

  ngOnInit() {
    this.getState.subscribe((state) => {
      this.isAuthenticated = state.isAuthenticated;
      this.user = state.user;
      this.currentUrl = this.router.url.split('/')[1].split('?')[0];
    });
  }

  logout() {
    this.store.dispatch(new LogOut);
  }

}
