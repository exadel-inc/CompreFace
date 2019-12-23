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
import { Application } from 'src/app/data/application';

@Injectable()
export class ApplciationListEffect {
  constructor(private actions: Actions, private http: HttpClient) { }

  @Effect()
  fetchApplicationList: Observable<FetchApplicationListSuccess | FetchApplicationListFail> = this.actions.pipe(
    ofType(ApplicationListTypes.FETCH_APPLICATION),
    switchMap((action: FetchApplicationList) =>
      // fetch service call here
      this.http.get<Application>(`http://localhost:3000/org/${action.payload.organizationId}/apps`).pipe(
        map(apps => new FetchApplicationListSuccess({ applicationList: apps })),
        catchError(e => of(new FetchApplicationListFail({ errorMessage: e })))
      )
    )
  )

  @Effect()
  createApplication: Observable<CreateApplicationSuccess | CreateApplicationFail> = this.actions.pipe(
    ofType(ApplicationListTypes.CREATE_APPLICATION),
    switchMap((action: CreateApplication) => {
      return this.http.post<Application>(`http://localhost:3000/org/${action.payload.organizationId}/app`, { name: action.payload.name })
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
