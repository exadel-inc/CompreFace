import {Store} from '@ngrx/store';
import {AppState} from 'src/app/store';
import {AuthService} from './auth.service';
import {updateUserInfo} from '../../store/userInfo/action';

export class AuthInit {
  constructor(private store: Store<AppState>, private auth: AuthService) {}

  public init(): void {
    const token: string = this.auth.getToken();
    const username: string = localStorage.getItem('username');

    if (token) {
      if (this.auth.isTokenValid(token)) {
        this.store.dispatch(updateUserInfo({
          isAuthenticated: true,
          username
        }));
      }
    }
  }
}
