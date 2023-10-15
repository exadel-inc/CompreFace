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
import { Actions, Effect, ofType } from '@ngrx/effects';
import { Store } from '@ngrx/store';
import { of } from 'rxjs';
import { catchError, map, switchMap, tap } from 'rxjs/operators';
import { ApplicationService } from 'src/app/core/application/application.service';
import { Routes } from 'src/app/data/enums/routers-url.enum';
import { SnackBarService } from 'src/app/features/snackbar/snackbar.service';

import {
  createApplication,
  createApplicationFail,
  createApplicationSuccess,
  deleteApplication,
  deleteApplicationFail,
  deleteApplicationSuccess,
  loadApplications,
  loadApplicationsFail,
  loadApplicationsSuccess,
  setSelectedAppIdEntityAction,
  updateApplication,
  updateApplicationFail,
  updateApplicationSuccess,
} from './action';

@Injectable()
export class ApplicationListEffect {
  constructor(
    private actions: Actions,
    private applicationService: ApplicationService,
    private snackBarService: SnackBarService,
    private router: Router,
    private store: Store<any>
  ) { }

  @Effect()
  loadApplications$ = this.actions.pipe(
    ofType(loadApplications),
    switchMap(() =>
      this.applicationService.getAll().pipe(
        map(applications => loadApplicationsSuccess({ applications })),
        catchError(error => of(loadApplicationsFail({ error })))
      )
    )
  );

  @Effect()
  createApplication$ = this.actions.pipe(
    ofType(createApplication),
    switchMap(({ name }) =>
      this.applicationService.create(name).pipe(
        map(application => createApplicationSuccess({ application })),
        catchError(error => of(createApplicationFail({ error })))
      )
    )
  );

  @Effect({ dispatch: false })
  createApplicationSuccess$ = this.actions.pipe(
    ofType(createApplicationSuccess),
    tap(({ application }) => {
      this.store.dispatch(setSelectedAppIdEntityAction({ selectedAppId: application.id }));
      this.snackBarService.openNotification({ messageText: 'application.created' });
    })
  );

  @Effect()
  updateApplication$ = this.actions.pipe(
    ofType(updateApplication),
    switchMap(({ id, name }) =>
      this.applicationService.put(id, name).pipe(
        map(application => updateApplicationSuccess({ application })),
        catchError(error => of(updateApplicationFail({ error })))
      )
    )
  );

  @Effect({ dispatch: false })
  updateApplicationSuccess$ = this.actions.pipe(
    ofType(updateApplicationSuccess),
    tap(() => {
      this.snackBarService.openNotification({ messageText: 'application.edited' });
    })
  )

  @Effect()
  deleteApplication$ = this.actions.pipe(
    ofType(deleteApplication),
    switchMap(app =>
      this.applicationService.delete(app.id).pipe(
        switchMap(() => {
          this.router.navigate([`${Routes.Home}`]);
          return [deleteApplicationSuccess({ id: app.id }), setSelectedAppIdEntityAction({ selectedAppId: null })];
        }),
        catchError(error => of(deleteApplicationFail({ error })))
      )
    )
  );

  @Effect({ dispatch: false })
  deleteApplicationSuccess$ = this.actions.pipe(
    ofType(deleteApplicationSuccess),
    tap(() => {
      this.snackBarService.openNotification({ messageText: 'application.deleted' });
    })
  )

  @Effect({ dispatch: false })
  showError$ = this.actions.pipe(
    ofType(loadApplicationsFail, createApplicationFail, updateApplicationFail, deleteApplicationFail),
    tap(action => {
      this.snackBarService.openHttpError(action.error);
    })
  );
}
