import { Injectable } from '@angular/core';
import { Resolve } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { take } from 'rxjs/operators';

import { AppState } from '../../store';
import { selectUserId } from '../../store/userInfo/selectors';
import { getUserInfo } from '../../store/userInfo/action';

@Injectable({ providedIn: 'root' })
export class UserInfoResolver implements Resolve<boolean> {
  loggedUserId$: Observable<string>;

  constructor(private store: Store<AppState>) {
    this.loggedUserId$ = this.store.select(selectUserId);
  }

  resolve(): Promise<boolean> {
    return new Promise<boolean>(resolve => {
      this.loggedUserId$.pipe(take(1)).subscribe(userId => {
        if (!userId) {
          this.store.dispatch(getUserInfo());
        }
        return resolve(true);
      });
    });
  }
}
