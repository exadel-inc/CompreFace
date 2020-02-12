import {Component, OnInit} from '@angular/core';
import {AppState} from '../../store';
import {Store} from '@ngrx/store';
import {Observable} from 'rxjs';
import {logOut} from '../../store/auth/action';
import {selectAuthState} from '../../store/auth/selectors';
import {selectUserAvatar} from '../../store/userInfo/selectors';

@Component({
  selector: 'app-tool-bar',
  templateUrl: './tool-bar.component.html',
  styleUrls: ['./tool-bar.component.sass']
})
export class ToolBarComponent implements OnInit {
  getState$: Observable<any>;
  userAvatarInfo$: Observable<string>;
  isAuthenticated: false;
  user = null;

  constructor( private store: Store<AppState>) {
    this.getState$ = this.store.select(selectAuthState);
    this.userAvatarInfo$ = this.store.select(selectUserAvatar);
  }

  ngOnInit() {
    this.getState$.subscribe((state) => {
      this.isAuthenticated = state.isAuthenticated;
      this.user = state.user;
    });
  }

  logout() {
    this.store.dispatch(logOut());
  }

}
