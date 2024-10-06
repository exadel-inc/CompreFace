/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable, merge } from 'rxjs';
import { Routes } from 'src/app/data/enums/routers-url.enum';
import { logOut, changePassword, changePasswordSuccess, changePasswordFail } from 'src/app/store/auth/action';
import { editUserInfo } from 'src/app/store/userInfo/action';
import { loadDemoApiKeySuccess } from 'src/app/store/demo/action';
import { selectUserAvatar, selectUserFirstName, selectUserId, selectUserLastName } from 'src/app/store/userInfo/selectors';

import { AppState } from '../../store';
import { ChangePassword } from '../../data/interfaces/change-password';
import { EditUserInfo } from '../../data/interfaces/edit-user-info';
import { selectImageCollection } from 'src/app/store/manage-collectiom/selectors';
import { CollectionItem } from 'src/app/data/interfaces/collection';
import { Actions, ofType } from '@ngrx/effects';
import { map } from 'rxjs/operators';

@Injectable()
export class ToolBarFacade {
  userAvatarInfo$: Observable<string>;
  userFirstName$: Observable<string>;
  userLastName$: Observable<string>;
  isUserInfoAvailable$: Observable<string>;
  passwordChangeResult$: Observable<string>;
  collectionItems$: Observable<CollectionItem[]>;

  constructor(private store: Store<AppState>, private router: Router, private actions: Actions) {
    this.userAvatarInfo$ = this.store.select(selectUserAvatar);
    this.userFirstName$ = this.store.select(selectUserFirstName);
    this.userLastName$ = this.store.select(selectUserLastName);
    this.isUserInfoAvailable$ = this.store.select(selectUserId);
    this.collectionItems$ = this.store.select(selectImageCollection);
    const success$: Observable<string> = this.actions.pipe(
      ofType(changePasswordSuccess),
      map(() => 'success')
    );

    const fail$: Observable<string> = this.actions.pipe(
      ofType(changePasswordFail),
      map(() => 'error')
    );

    this.passwordChangeResult$ = merge(success$, fail$);
  }

  goSignUp() {
    this.store.dispatch(loadDemoApiKeySuccess(null));
    this.router.navigateByUrl(Routes.SignUp);
  }

  logout() {
    this.store.dispatch(logOut());
  }

  changePassword(payload: ChangePassword) {
    this.store.dispatch(changePassword(payload));
  }

  editUserInfo(payload: EditUserInfo) {
    this.store.dispatch(editUserInfo(payload));
  }
}
