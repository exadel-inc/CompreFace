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
import { delay, retryWhen, switchMap } from 'rxjs/operators';
import { LoadingPhotoService } from 'src/app/core/photo-loader/photo-loader.service';
import { ServerStatusService } from 'src/app/core/server-status/server-status.service';
import { ServerStatus } from 'src/app/data/enums/servers-status';
import { DemoStatus } from 'src/app/data/interfaces/demo-status';
import { DemoService } from 'src/app/pages/demo/demo.service';
import {
  getBeServerStatus,
  getBeServerStatusSuccess,
  getCoreServerStatus,
  getCoreServerStatusSuccess,
  getDbServerStatus,
  getDbServerStatusSuccess,
} from './actions';
import { ServerStatusInt } from './reducers';

@Injectable()
export class ServerStatusEffect {
  delayTime: number = 10000;

  constructor(
    private actions: Actions,
    private statusService: ServerStatusService,
    private dbService: DemoService,
    private coreService: LoadingPhotoService
  ) {}

  @Effect()
  $getServerStatus = this.actions.pipe(
    ofType(getBeServerStatus),
    switchMap(() =>
      this.statusService.getServerStatus().pipe(
        switchMap((status: ServerStatusInt) => {
          if (status.status === ServerStatus.Ready) {
            return [getBeServerStatusSuccess()];
          }
          return [getBeServerStatus()];
        }),
        retryWhen(err => err.pipe(delay(this.delayTime)))
      )
    )
  );

  @Effect()
  $getDbServerStatus = this.actions.pipe(
    ofType(getDbServerStatus),
    switchMap(() =>
      this.dbService.getStatus().pipe(
        switchMap((status: DemoStatus) => {
          if (!status?.dbIsInconsistent) {
            return [getDbServerStatusSuccess()];
          }
          return [getDbServerStatus()];
        }),
        retryWhen(err => err.pipe(delay(this.delayTime)))
      )
    )
  );

  @Effect()
  $getCoreServerStatus = this.actions.pipe(
    ofType(getCoreServerStatus),
    switchMap(() =>
      this.coreService.getPlugin().pipe(
        switchMap(status => {
          if (status.status === ServerStatus.Ready) {
            return [getCoreServerStatusSuccess()];
          }
          return [getCoreServerStatus()];
        }),
        retryWhen(err => err.pipe(delay(this.delayTime)))
      )
    )
  );
}
