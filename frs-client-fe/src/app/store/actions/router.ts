import { Action } from '@ngrx/store';
import { NavigationExtras } from '@angular/router';


// todo: it was added for example
export enum RouterActionTypes {
  GO = '[Router] Go',
  BACK = '[Router] Back',
  FORWARD = '[Router] Forward'
}

export class Go implements Action {
  readonly type = RouterActionTypes.GO;
  constructor(
    public payload: {
      path: any[];
      query?: object;
      extras?: NavigationExtras;
    }
  ) {}
}

export class Back implements Action {
  readonly type = RouterActionTypes.BACK;
}

export class Forward implements Action {
  readonly type = RouterActionTypes.FORWARD;
}

export type All =
  | Go
  | Back
  | Forward;
