import {Action} from '@ngrx/store';

export enum ApplicationListTypes {
  FETCH_APPLICATION = '[Application List] Fetch Applications',
  FETCH_APPLICATION_SUCCESS = '[Application List] Fetch Applications Success',
  FETCH_AFETCH_APPLICATION_FAIL= '[Application List] Fetch Applications Fail'
}

export class FetchApplicationList implements Action {
  readonly type = ApplicationListTypes.FETCH_APPLICATION;
  constructor() {}
}

export class FetchApplicationListSuccess implements Action {
  readonly type = ApplicationListTypes.FETCH_APPLICATION_SUCCESS;
  constructor(public payload: any) {}
}

export class FetchApplicationListFail implements Action {
  readonly type = ApplicationListTypes.FETCH_AFETCH_APPLICATION_FAIL;
  constructor(public payload: any) {}
}

export type ApplicationListActions =
  | FetchApplicationList
  | FetchApplicationListSuccess
  | FetchApplicationListFail;
