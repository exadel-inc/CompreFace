import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { Observable, of } from 'rxjs';
import { map, switchMap, catchError } from 'rxjs/operators';
import { ApplicationListTypes, FetchApplicationListSuccess, FetchApplicationListFail } from './action';
import { HttpClient } from '@angular/common/http';
import { Application } from 'src/app/data/application';

@Injectable()
export class ApplciationListEffect {
  constructor(private actions: Actions, private http: HttpClient) { }

  @Effect()
  fetchApplicationList: Observable<any> = this.actions.pipe(
    ofType(ApplicationListTypes.FETCH_APPLICATION),
    switchMap(() =>
      // fetch service call here
     this.http.get<Application>('http://localhost:3000/apps/org/0').pipe(
        map(apps => new FetchApplicationListSuccess({ applicationList: apps })),
        catchError(e => of(new FetchApplicationListFail( {errorMessage: e} )))
      )
    )
  )
}
