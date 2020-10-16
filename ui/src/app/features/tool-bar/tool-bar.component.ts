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

import {Component, Input, OnInit} from '@angular/core';
import {AppState} from '../../store';
import {Store} from '@ngrx/store';
import {Observable} from 'rxjs';
import {logOut} from '../../store/auth/action';
import {selectAuthState} from '../../store/auth/selectors';
import {selectUserAvatar} from '../../store/userInfo/selectors';
import { UserInfoService } from 'src/app/core/user-info/user-info.service';

@Component({
  selector: 'app-tool-bar',
  templateUrl: './tool-bar.component.html',
  styleUrls: ['./tool-bar.component.scss']
})
export class ToolBarComponent implements OnInit {
  @Input () userInfo: string;
  getState$: Observable<any>;
  userAvatarInfo$: Observable<string>;
  isAuthenticated: false;
  user = null;

  constructor( private store: Store<AppState>, private userInfoService: UserInfoService) {
    this.getState$ = this.store.select(selectAuthState);
    this.userAvatarInfo$ = this.store.select(selectUserAvatar);
  }

  ngOnInit() {
    this.getState$.subscribe((state) => {
      this.isAuthenticated = state.isAuthenticated;
      this.user = state.user;
    });
    this.getUserInfo();
  }

  getUserInfo() {
    this.userInfoService.get().subscribe((data: any) => {
      this.userInfo = (data.firstName + ' ' + data.lastName);
      console.log(this.userInfo);
    });
  }

  logout() {
    this.store.dispatch(logOut());
  }

}
