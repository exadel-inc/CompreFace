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
import { Observable } from 'rxjs';
import { Routes } from 'src/app/data/enums/routers-url.enum';
import { logOut, changePassword } from 'src/app/store/auth/action';
import { editUserInfo } from 'src/app/store/userInfo/action';
import { loadDemoApiKeySuccess } from 'src/app/store/demo/action';
import { selectDemoPageAvailability } from 'src/app/store/demo/selectors';
import { selectUserAvatar, selectUserName } from 'src/app/store/userInfo/selectors';

import { AppState } from '../../store';
import { ChangePassword } from '../../data/interfaces/change-password';
import { EditUserInfo } from '../../data/interfaces/edit-user-info';

@Injectable()
export class ToolBarFacade {
  userAvatarInfo$: Observable<string>;
  userName$: Observable<string>;
  isUserInfoAvailable$: Observable<boolean>;

  constructor(private store: Store<AppState>, private router: Router) {
    this.userAvatarInfo$ = this.store.select(selectUserAvatar);
    this.userName$ = this.store.select(selectUserName);
    this.isUserInfoAvailable$ = this.store.select(selectDemoPageAvailability);
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
