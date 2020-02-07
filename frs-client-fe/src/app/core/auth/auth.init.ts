import {Store} from '@ngrx/store';
import {AppState} from 'src/app/store';
import {AuthService} from './auth.service';
import {UpdateUserInfo} from '../../store/userInfo/action';

export class AuthInit {
  constructor(private store: Store<AppState>, private auth: AuthService) {
    const token: string = this.auth.getToken();
    const username: string = localStorage.getItem('username');

    if (token) {
      if (this.auth.isTokenValid(token)) {
        this.store.dispatch(new UpdateUserInfo({
          isAuthenticated: true,
          username: username
        }));
      }
    }
  }
}
