import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { switchMap, catchError } from 'rxjs/operators';
import {
  ApplicationListTypes,
  FetchApplicationList,
  FetchApplicationListSuccess,
  FetchApplicationListFail,
  CreateApplication,
  CreateApplicationSuccess,
  CreateApplicationFail, UpdateApplication
} from './action';
import { ApplicationService } from 'src/app/core/application/application.service';
import {addApplication, addApplications, updateApplication} from "../application/action";

@Injectable()
export class ApplicationListEffect {
  constructor(private actions: Actions, private applicationService: ApplicationService) { }

  @Effect()
  fetchApplicationList = this.actions.pipe(
    ofType(ApplicationListTypes.FETCH_APPLICATION),
    switchMap((action: FetchApplicationList) => {
      return this.applicationService.getAll(action.payload.organizationId).pipe(
        switchMap(apps =>
          [
            new FetchApplicationListSuccess(),
            addApplications({ applications: apps })
          ]),
        catchError(e => of(new FetchApplicationListFail({ errorMessage: e })))
      )
    })
  );

  @Effect()
  createApplication = this.actions.pipe(
    ofType(ApplicationListTypes.CREATE_APPLICATION),
    switchMap((action: CreateApplication) => {
      return this.applicationService.create(action.payload.organizationId, action.payload.name)
        .pipe(
          switchMap((app) => [
            new CreateApplicationSuccess(),
            addApplication({ application: app })
          ]),
          catchError(error => of(new CreateApplicationFail({ errorMessage: error })))
        )
    })
  );

  @Effect()
  updateApplication = this.actions.pipe(
    ofType(ApplicationListTypes.UPDATE_APPLICATION),
    switchMap((action: UpdateApplication) => {
      return this.applicationService.put(action.payload)
        .pipe(
          switchMap((app) => [
            new CreateApplicationSuccess(),
            updateApplication({ application: app })
          ]),
          catchError(error => of(new CreateApplicationFail({ errorMessage: error })))
        )
    })
  )
}
