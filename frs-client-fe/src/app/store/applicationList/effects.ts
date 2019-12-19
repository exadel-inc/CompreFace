import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ApplicationListTypes, FetchApplicationListSuccess } from './action';

const applicationListMock = [
  {
    id: '0',
    name: 'Application 0',
    owner: 'Owner 0'
  },
  {
    id: '1',
    name: 'Application 1',
    owner: 'Owner 1'
  },
  {
    id: '2',
    name: 'Application 2',
    owner: 'Owner 2'
  },
  {
    id: '3',
    name: 'Application 3',
    owner: 'Owner 3'
  }
];

@Injectable()
export class ApplciationListEffect {
  constructor(private actions: Actions) { }

  @Effect()
  fetchApplicationList: Observable<any> = this.actions.pipe(
    ofType(ApplicationListTypes.FETCH_APPLICATION),
    map(() =>
      // fetch service call here

      new FetchApplicationListSuccess({
        applicationList: applicationListMock
      })
    )
  )
}
