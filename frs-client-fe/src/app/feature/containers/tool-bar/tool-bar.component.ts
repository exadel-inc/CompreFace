import { Component, OnInit } from '@angular/core';
import {AppState} from "../../../store/state/app.state";
import {Store} from "@ngrx/store";
import {LogOut} from "../../../store/actions/auth";

@Component({
  selector: 'app-tool-bar',
  templateUrl: './tool-bar.component.html',
  styleUrls: ['./tool-bar.component.sass']
})
export class ToolBarComponent implements OnInit {

  constructor( private store: Store<AppState>) { }

  ngOnInit() {
  }

  logout() {
    console.log('logout');
    this.store.dispatch(new LogOut);
  }

}
