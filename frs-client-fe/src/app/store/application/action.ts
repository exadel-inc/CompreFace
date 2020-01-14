import { Action } from '@ngrx/store';
import { Application } from 'src/app/data/application';

export enum ApplicationEntityActionList {
  ADD_APPLICATION = '[Application/API] Add Application',
  ADD_APPLICATIONS = '[Application/API] Add Applications',
  REMOVE_APPLICATION = '[Application/API] Remove Application',
  REMOVE_APPLICATIONS = '[Application/API] Remove Applications',
  UPDATE_APPLICATION = '[Application/API] Update Application',
  SET_SELECTED_ID = '[Application/API] Set Selected ID Application',
}

export class AddApplicationEntityAction implements Action {
  readonly type = ApplicationEntityActionList.ADD_APPLICATION;
  constructor(public payload: {
    application: Application
  }) {}
}

export class AddApplicationsEntityAction implements Action {
  readonly type = ApplicationEntityActionList.ADD_APPLICATIONS;
  constructor(public payload: {
    applications: Application[]
  }) {}
}

export type ApplicationEntityActionType =
  | AddApplicationEntityAction
  | AddApplicationsEntityAction;
