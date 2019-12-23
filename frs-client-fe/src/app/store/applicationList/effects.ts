import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { Observable, of } from 'rxjs';
import { map, switchMap, catchError } from 'rxjs/operators';
import {
  ApplicationListTypes,
  FetchApplicationList,
  FetchApplicationListSuccess,
  FetchApplicationListFail,
  CreateApplication,
  CreateApplicationSuccess,
  CreateApplicationFail
} from './action';
import { HttpClient } from '@angular/common/http';
import { ApplicationService } from 'src/app/core/application/application.service';

@Injectable()
export class ApplciationListEffect {
  constructor(private actions: Actions, private applicationService: ApplicationService, private http: HttpClient) { }

  @Effect()
  fetchApplicationList: Observable<FetchApplicationListSuccess | FetchApplicationListFail> = this.actions.pipe(
    ofType(ApplicationListTypes.FETCH_APPLICATION),
    switchMap((action: FetchApplicationList) =>
      this.applicationService.getAll(action.payload.organizationId).pipe(
        map(apps => new FetchApplicationListSuccess({ applicationList: apps })),
        catchError(e => of(new FetchApplicationListFail({ errorMessage: e })))
      )
    )
  )

  @Effect()
  createApplication: Observable<CreateApplicationSuccess | CreateApplicationFail> = this.actions.pipe(
    ofType(ApplicationListTypes.CREATE_APPLICATION),
    switchMap((action: CreateApplication) => {
      return this.applicationService.create(action.payload.organizationId, action.payload.name)
        .pipe(
          map(() => new CreateApplicationSuccess({ organizationId: action.payload.organizationId })),
          catchError(error => of(new CreateApplicationFail({ errorMessage: error })))
        )
    })
  )

  @Effect()
  CreateApplicationSuccess: Observable<FetchApplicationList> = this.actions.pipe(
    ofType(ApplicationListTypes.CREATE_APPLICATION_SUCCESS),
    map((action: CreateApplicationSuccess) => new FetchApplicationList({
      organizationId: action.payload.organizationId
    }))
  )
}
