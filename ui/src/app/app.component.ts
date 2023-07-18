/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
import { Component, OnInit, OnDestroy } from '@angular/core';
import { Store } from '@ngrx/store';
import { TranslateService } from '@ngx-translate/core';

import { AuthService } from './core/auth/auth.service';
import { AppState } from './store';
import { CustomIconsService } from './core/custom-icons/custom-icons.service';
import { getMaxImageSize } from './store/image-size/actions';
import { refreshToken } from './store/auth/action';
import { GranTypes } from './data/enums/gran_type.enum';
import { selectUserId } from './store/userInfo/selectors';
import { Observable, Subject } from 'rxjs';
import { filter, tap, takeUntil } from 'rxjs/operators';
import { getPlugin } from './store/landmarks-plugin/action';
import { getMailServiceStatus } from './store/mail-service/actions';
import { getBeServerStatus, getCoreServerStatus, getDbServerStatus } from './store/servers-status/actions';
import { selectServerStatus } from './store/servers-status/selectors';
import { ServerStatusInt } from './store/servers-status/reducers';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit, OnDestroy {
  userId$: Observable<string>;
  serverStatus$: Observable<ServerStatusInt>;

  serverStatus: ServerStatusInt;
  unsubscribe$ = new Subject<void>();

  constructor(
    auth: AuthService,
    private store: Store<AppState>,
    private translate: TranslateService,
    private customIconsService: CustomIconsService
  ) {
    translate.setDefaultLang('en');
    customIconsService.registerIcons();
    this.userId$ = this.store.select(selectUserId);
    this.serverStatus$ = this.store.select(selectServerStatus).pipe(filter(status => !!status));
  }

  ngOnInit(): void {
    this.store.dispatch(getBeServerStatus({ preserveState: false }));
    this.store.dispatch(getDbServerStatus({ preserveState: false }));
    this.store.dispatch(getCoreServerStatus({ preserveState: false }));

    this.serverStatus$.pipe(takeUntil(this.unsubscribe$)).subscribe(status => {
      this.serverStatus = status;
      if (status.coreStatus && status.apiStatus && status.status) {
        this.getUserId();
      }
    });
  }

  getUserId(): void {
    this.userId$
      .pipe(
        filter(userId => !!userId),
        tap(() => {
          this.store.dispatch(getMaxImageSize());
          this.store.dispatch(getPlugin());
          const payload = {
            grant_type: GranTypes.RefreshToken,
            scope: 'all',
          };
          this.store.dispatch(getMailServiceStatus());
          setInterval(() => this.store.dispatch(refreshToken(payload)), 300000);
        }),
        takeUntil(this.unsubscribe$)
      )
      .subscribe();
  }

  ngOnDestroy(): void {
    this.unsubscribe$.next();
    this.unsubscribe$.complete();
  }
}
