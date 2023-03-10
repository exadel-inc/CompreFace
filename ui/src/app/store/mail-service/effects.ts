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
import { of } from 'rxjs';
import { catchError, switchMap, map } from 'rxjs/operators';
import { MailService } from 'src/app/core/mail-service/mail-service.service';
import { MailServiceStatus } from 'src/app/data/interfaces/mail-service-status';
import { getMailServiceStatus, getMailServiceStatusFail, getMailServiceStatusSuccess } from './actions';

@Injectable()
export class MailServiceEffects {
  constructor(private mailService: MailService, private actions: Actions, private store: Store<any>) {}

  @Effect({ dispatch: false })
  getMailServiceStatus$ = this.actions.pipe(
    ofType(getMailServiceStatus),
    switchMap(() =>
      this.mailService.getStatus().pipe(
        map((status: MailServiceStatus) => this.store.dispatch(getMailServiceStatusSuccess(status))),
        catchError(error => of(getMailServiceStatusFail(error)))
      )
    )
  );
}
