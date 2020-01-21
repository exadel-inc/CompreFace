import { Action } from '@ngrx/store';

export enum ApplicationListTypes {
  FETCH_APPLICATION = '[Application List] Fetch Applications',
  FETCH_APPLICATION_SUCCESS = '[Application List] Fetch Applications Success',
  FETCH_APPLICATION_FAIL= '[Application List] Fetch Applications Fail',
  CREATE_APPLICATION = '[Application List] Create Application',
  UPDATE_APPLICATION = '[Application List] Update Application',
  CREATE_APPLICATION_SUCCESS = '[Application List] Create Application Success',
  CREATE_APPLICATION_FAIL = '[Application List] Create Application Fail',
  UPDATE_APPLICATION_SUCCESS = '[Application List] Update Application Success',
  UPDATE_APPLICATION_FAIL = '[Application List] Update Application Fail'
}

export class FetchApplicationList implements Action {
  readonly type = ApplicationListTypes.FETCH_APPLICATION;
  constructor(public payload: {
    organizationId: string
  }) {}
}

export class FetchApplicationListSuccess implements Action {
  readonly type = ApplicationListTypes.FETCH_APPLICATION_SUCCESS;
  constructor() {}
}

export class FetchApplicationListFail implements Action {
  readonly type = ApplicationListTypes.FETCH_APPLICATION_FAIL;
  constructor(public payload: {
    errorMessage: string
  }) {}
}

export class CreateApplication implements Action {
  readonly type = ApplicationListTypes.CREATE_APPLICATION;
  constructor(public payload: {
    organizationId: string,
    name: string
  }) {}
}

export class UpdateApplication implements Action {
  readonly type = ApplicationListTypes.UPDATE_APPLICATION;
  constructor(public payload: {
    organizationId: string,
    name: string,
    appId: string
  }) {}
}

export class CreateApplicationSuccess implements Action {
  readonly type = ApplicationListTypes.CREATE_APPLICATION_SUCCESS;
  constructor() {}
}

export class CreateApplicationFail implements Action {
  readonly type = ApplicationListTypes.CREATE_APPLICATION_FAIL;
  constructor(public payload: {
    errorMessage: string
  }) {}
}

export class UpdateApplicationSuccess implements Action {
  readonly type = ApplicationListTypes.UPDATE_APPLICATION_SUCCESS;
  constructor() {}
}

export class UpdateApplicationFail implements Action {
  readonly type = ApplicationListTypes.UPDATE_APPLICATION_FAIL;
  constructor(public payload: {
    errorMessage: string
  }) {}
}

export type ApplicationListActions =
  | FetchApplicationList
  | FetchApplicationListSuccess
  | FetchApplicationListFail
  | CreateApplication
  | UpdateApplication
  | CreateApplicationSuccess
  | CreateApplicationFail
  | UpdateApplicationSuccess
  | UpdateApplicationFail
