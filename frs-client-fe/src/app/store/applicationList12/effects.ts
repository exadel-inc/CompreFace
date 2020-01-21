import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { switchMap, catchError, map } from 'rxjs/operators';
import {
  ApplicationListTypes,
  FetchApplicationList,
  FetchApplicationListSuccess,
  FetchApplicationListFail,
  CreateApplication,
  CreateApplicationSuccess,
  CreateApplicationFail,
  UpdateApplication,
  UpdateApplicationSuccess,
  UpdateApplicationFail
} from './action';
import { ApplicationService } from 'src/app/core/application/application.service';
import {
  addApplicationEntityAction,
  addApplicationsEntityAction,
  createApplicationEntityAction,
  updateApplicationEntityAction,
  putUpdatedApplicationEntityAction,
  loadApplicationsEntityAction
} from "../application/action";

@Injectable()
export class ApplicationListEffect {
  constructor(private actions: Actions, private applicationService: ApplicationService) { }

  @Effect()
  fetchApplicationList = this.actions.pipe(
    ofType(loadApplicationsEntityAction),
    switchMap((action) => this.applicationService.getAll(action.organizationId)),
    map(apps => addApplicationsEntityAction({ applications: apps }))
  );

  @Effect()
  createApplication = this.actions.pipe(
    ofType(createApplicationEntityAction),
    switchMap((action) => this.applicationService.create(action.organizationId, action.name)),
    map((app) => addApplicationEntityAction({ application: app }))
  );

  @Effect()
  updateApplication = this.actions.pipe(
    ofType(putUpdatedApplicationEntityAction),
    switchMap((action) => this.applicationService.put(
      action.application.organizationId,
      action.application.id,
      action.application.name
    )),
    map((app) => updateApplicationEntityAction({ application: app }))
  )
}
