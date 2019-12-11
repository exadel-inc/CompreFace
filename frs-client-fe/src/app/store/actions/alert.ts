import {Action} from '@ngrx/store';

export enum AlertActionTypes {
  SUCCESS = '[Alert] Success',
  WARNING= '[Alert] Warning',
  DANGER = '[Alert] Danger',
}

export class AlertSuccess implements Action {
  readonly type = AlertActionTypes.SUCCESS;
  constructor(public payload: any) {
  }
}

export class AlertWarning implements Action {
  readonly type = AlertActionTypes.WARNING;
  constructor(public payload: any) {
  }
}

export class AlertDanger implements Action {
  readonly type = AlertActionTypes.DANGER;
  constructor(public payload: any) {
  }
}


export type All =
  | AlertSuccess
  | AlertWarning
  | AlertDanger;
