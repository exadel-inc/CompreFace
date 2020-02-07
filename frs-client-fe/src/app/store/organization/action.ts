import {Action} from '@ngrx/store';

export enum OrganizationActionTypes {
  GET_SELECTED_ID = '[Organization] Get Selected Id',
  SET_SELECTED_ID = '[Organization] Set Selected Id',
}

export class GetSelectedId implements Action {
  readonly type = OrganizationActionTypes.GET_SELECTED_ID;
  constructor() {}
}

export class SetSelectedId implements Action {
  readonly type = OrganizationActionTypes.SET_SELECTED_ID;
  constructor(public payload: any) {}
}

export type OrganizationActions =
  | GetSelectedId
  | SetSelectedId;

