import {Injectable} from '@angular/core';
import {Actions, Effect, ofType} from '@ngrx/effects';
import {switchMap, map} from 'rxjs/operators';
import {ApplicationService} from 'src/app/core/application/application.service';
import {
  addApplicationEntityAction,
  addApplicationsEntityAction,
  createApplicationEntityAction,
  updateApplicationEntityAction,
  putUpdatedApplicationEntityAction,
  loadApplicationsEntityAction
} from './action';

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
      action.organizationId,
      action.id,
      action.name
    )),
    map((app) => updateApplicationEntityAction({ application: app }))
  );
}
