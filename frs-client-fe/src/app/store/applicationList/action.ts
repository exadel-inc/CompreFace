import {Action} from '@ngrx/store';

export enum ApplicationListTypes {
  FETCH_APPLICATION = '[Application List] Fetch Applications',
  FETCH_APPLICATION_SUCCESS = '[Application List] Fetch Applications Success',
  FETCH_AFETCH_APPLICATION_FAIL= '[Application List] Fetch Applications Fail',
  CREATE_APPLICATION = '[Application List] Create Application',
  CREATE_APPLICATION_SUCCESS = '[Application List] Create Application Success',
  CREATE_APPLICATION_FAIL = '[Application List] Create Application Fail'
}

export class FetchApplicationList implements Action {
  readonly type = ApplicationListTypes.FETCH_APPLICATION;
  constructor(public payload: {
    organizationId: string
  }) {}
}

export class FetchApplicationListSuccess implements Action {
  readonly type = ApplicationListTypes.FETCH_APPLICATION_SUCCESS;
  constructor(public payload: any) {}
}

export class FetchApplicationListFail implements Action {
  readonly type = ApplicationListTypes.FETCH_AFETCH_APPLICATION_FAIL;
  constructor(public payload: any) {}
}

export class CreateApplication implements Action {
  readonly type = ApplicationListTypes.CREATE_APPLICATION;
  constructor(public payload: {
    organizationId: string,
    name: string
  }) {}
}

export class CreateApplicationSuccess implements Action {
  readonly type = ApplicationListTypes.CREATE_APPLICATION_SUCCESS;
  constructor(public payload: {
    organizationId: string
  }) {}
}

export class CreateApplicationFail implements Action {
  readonly type = ApplicationListTypes.CREATE_APPLICATION_FAIL;
  constructor(public payload: {
    errorMessage: string
  }) {}
}

export type ApplicationListActions =
  | FetchApplicationList
  | FetchApplicationListSuccess
  | FetchApplicationListFail
  | CreateApplicationSuccess
  | CreateApplicationFail;
