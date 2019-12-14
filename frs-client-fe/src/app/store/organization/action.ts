import {Action} from '@ngrx/store';
import {Organization} from "../../data/organization";
import {State} from "./reducers";

export enum OrganizationActionTypes {
  GET_ALL= '[Organization] Get All',
  GET_ALL_SUCCESS= '[Organization] Get All Success',
  GET_ONE= '[Organization] Get One',
  CREATE= '[Organization] Create One',
}

export class GetAll implements Action {
  readonly type = OrganizationActionTypes.GET_ALL;
  constructor() {
  }
}

export class GetOne implements Action {
  readonly type = OrganizationActionTypes.GET_ONE;
  constructor(public payload: Organization) {}
}

export class Create implements Action {
  readonly type = OrganizationActionTypes.CREATE;
  constructor(public payload: Organization) {}
}

export class GetAllSuccess implements Action {
  readonly type = OrganizationActionTypes.GET_ALL_SUCCESS;
  constructor(public payload: { organizations: Organization[] }) {}
}




export type OrganizationActions =
  | GetAll
  | GetOne
  | Create
  | GetAllSuccess

