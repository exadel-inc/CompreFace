import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { Observable, of } from 'rxjs';
import { switchMap, catchError } from 'rxjs/operators';
import {
  ApplicationListTypes,
  FetchApplicationList,
  FetchApplicationListSuccess,
  FetchApplicationListFail,
  CreateApplication,
  CreateApplicationSuccess,
  CreateApplicationFail
} from './action';
import { AddApplicationEntityAction, AddApplicationsEntityAction } from '../application/action';
import { ApplicationService } from 'src/app/core/application/application.service';
import { Store } from '@ngrx/store';
import { AppState } from '..';

@Injectable()
export class ApplciationListEffect {
  constructor(private actions: Actions, private applicationService: ApplicationService, private store: Store<AppState>) { }

  @Effect()
  fetchApplicationList: Observable<FetchApplicationListSuccess | AddApplicationsEntityAction | FetchApplicationListFail> = this.actions.pipe(
    ofType(ApplicationListTypes.FETCH_APPLICATION),
    switchMap((action: FetchApplicationList) => {
      return this.applicationService.getAll(action.payload.organizationId).pipe(
        switchMap(apps =>
          [
            new FetchApplicationListSuccess(),
            new AddApplicationsEntityAction({ applications: apps })
          ]),
        catchError(e => of(new FetchApplicationListFail({ errorMessage: e })))
      )
    })
  )

  @Effect()
  createApplication: Observable<CreateApplicationSuccess | AddApplicationEntityAction | CreateApplicationFail> = this.actions.pipe(
    ofType(ApplicationListTypes.CREATE_APPLICATION),
    switchMap((action: CreateApplication) => {
      return this.applicationService.create(action.payload.organizationId, action.payload.name)
        .pipe(
          switchMap((app) => [
            new CreateApplicationSuccess(),
            new AddApplicationEntityAction({ application: app })
          ]),
          catchError(error => of(new CreateApplicationFail({ errorMessage: error })))
        )
    })
  )
}
