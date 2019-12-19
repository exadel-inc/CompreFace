import {Action, createAction, props} from '@ngrx/store';
import {EntityMap, Predicate, Update} from '@ngrx/entity';
import {Organization} from "../../data/organization";
import {LogIn, LogInSuccess} from "../auth/action";


// export const setSelectedId = createAction('[Organization/API] Set Select id', props<{ selectId: string | null}>());

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
  | SetSelectedId

