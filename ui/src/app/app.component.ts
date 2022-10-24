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
import { Component, OnInit } from '@angular/core';
import { Store } from '@ngrx/store';
import { TranslateService } from '@ngx-translate/core';

import { AuthService } from './core/auth/auth.service';
import { AppState } from './store';
import { CustomIconsService } from './core/custom-icons/custom-icons.service';
import { getMaxImageSize } from './store/image-size/actions';
import { refreshToken } from './store/auth/action';
import { GranTypes } from './data/enums/gran_type.enum';
import { selectUserId } from './store/userInfo/selectors';
import { Observable } from 'rxjs';
import { filter, map, tap } from 'rxjs/operators';
import { getPlugin } from './store/landmarks-plugin/action';
import { getBeServerStatus } from './store/servers-status/actions';
import { selectServerStatus } from './store/servers-status/selectors';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit {
  userId$: Observable<string>;
  serverStatus$: Observable<string>;

  constructor(
    auth: AuthService,
    private store: Store<AppState>,
    private translate: TranslateService,
    private customIconsService: CustomIconsService
  ) {
    translate.setDefaultLang('en');
    customIconsService.registerIcons();
    this.userId$ = this.store.select(selectUserId);
    this.serverStatus$ = this.store.select(selectServerStatus).pipe(
      tap(e => console.log(e, 'ss')),
      map(({ status }) => status)
    );
  }

  ngOnInit(): void {
    this.store.dispatch(getBeServerStatus());

    const subs = this.userId$
      .pipe(
        filter(userId => !!userId),
        tap(() => {
          this.store.dispatch(getMaxImageSize());
          this.store.dispatch(getPlugin());
          const payload = {
            grant_type: GranTypes.RefreshToken,
            scope: 'all',
          };
          setInterval(() => this.store.dispatch(refreshToken(payload)), 300000);
        })
      )
      .subscribe(() => subs.unsubscribe());
  }
}
