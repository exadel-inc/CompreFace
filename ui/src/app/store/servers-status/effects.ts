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
import { Actions, Effect, ofType } from '@ngrx/effects';
import { Store } from '@ngrx/store';
import { delay, retryWhen, switchMap } from 'rxjs/operators';
import { ServerStatusService } from 'src/app/core/server-status/server-status.service';
import { ServerStatus } from 'src/app/data/enums/servers-status';
import { getBeServerStatus, getBeServerStatusSuccess } from './actions';
import { ServerStatusInt } from './reducers';

@Injectable()
export class ServerStatusEffect {
  constructor(private actions: Actions, private store: Store<any>, private statusService: ServerStatusService) {}

  @Effect()
  $getServerStatus = this.actions.pipe(
    ofType(getBeServerStatus),
    switchMap(() =>
      this.statusService.getServerStatus().pipe(
        switchMap((status: ServerStatusInt) => {
          if (status.status === ServerStatus.Ready) {
            return [getBeServerStatusSuccess()];
          } else {
            return [getBeServerStatus()];
          }
        }),
        retryWhen(err => err.pipe(delay(5000)))
      )
    )
  );
}
