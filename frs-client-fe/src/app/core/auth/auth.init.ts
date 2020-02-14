import {Store} from '@ngrx/store';
import {AppState} from 'src/app/store';
import {AuthService} from './auth.service';
import {updateUserInfo} from '../../store/userInfo/action';

export class AuthInit {
  constructor(private store: Store<AppState>, private auth: AuthService) {}

  public init(): void {
    const token: string = this.auth.getToken();
    const firstName: string = localStorage.getItem('firstName');

    if (token) {
      if (this.auth.isTokenValid(token)) {
        this.store.dispatch(updateUserInfo({
          isAuthenticated: true,
          firstName
        }));
      }
    }
  }
}
