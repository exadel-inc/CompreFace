import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { catchError, map, switchMap, tap } from 'rxjs/operators';
import { ApplicationService } from 'src/app/core/application/application.service';
import { SnackBarService } from 'src/app/features/snackbar/snackbar.service';

import {
  createApplication,
  createApplicationFail,
  createApplicationSuccess,
  loadApplications,
  loadApplicationsFail,
  loadApplicationsSuccess,
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
  ) { }

  @Effect()
  loadApplications$ = this.actions.pipe(
    ofType(loadApplications),
    switchMap((action) => this.applicationService.getAll(action.organizationId).pipe(
      map(applications => loadApplicationsSuccess({ applications })),
      catchError(error => of(loadApplicationsFail({ error }))),
    )),
  );

  @Effect()
  createApplication$ = this.actions.pipe(
    ofType(createApplication),
    switchMap(({ organizationId, name }) => this.applicationService.create(organizationId, name).pipe(
      map(application => createApplicationSuccess({ application })),
      catchError(error => of(createApplicationFail({ error }))),
    )),
  );

  @Effect()
  updateApplication$ = this.actions.pipe(
    ofType(updateApplication),
    switchMap(({ organizationId, id, name }) => this.applicationService.put(organizationId, id, name).pipe(
      map(application => updateApplicationSuccess({ application })),
      catchError(error => of(updateApplicationFail({ error }))),
    )),
  );

  @Effect({ dispatch: false })
  showError$ = this.actions.pipe(
    ofType(loadApplicationsFail, createApplicationFail, updateApplicationFail),
    tap(action => {
      this.snackBarService.openHttpError(action.error);
    })
  );
}
