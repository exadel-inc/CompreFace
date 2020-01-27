import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import {
  loadAppUserEntityAction,
  addAppUserEntityAction,
  putUpdatedAppUserRoleEntityAction,
  updateUserRoleEntityAction
} from './actions';
import { loadApplicationsEntityAction } from 'src/app/store/application/action';
import { switchMap, map } from 'rxjs/operators';
import { AppUserService } from 'src/app/core/app-user/app-user.service';
import { forkJoin, of } from 'rxjs';

@Injectable()
export class AppUserEffects {
  constructor(private actions: Actions, private appUserService: AppUserService) {}

  @Effect()
  loadAppUsers = this.actions.pipe(
    ofType(loadAppUserEntityAction),
    switchMap(action => this.appUserService.getAll(action.organizationId, action.applicationId)),
    map(users => addAppUserEntityAction({ users }))
  );

  @Effect()
  updateAppUser = this.actions.pipe(
    ofType(putUpdatedAppUserRoleEntityAction),
    switchMap(action => forkJoin([this.appUserService.update(
      action.organizationId,
      action.applicationId,
      action.user.id,
      action.user.accessLevel
    ), of(action)])),
    switchMap(observableResult => {
      const [user, action] = observableResult;
      return [
        updateUserRoleEntityAction({ user }),
        loadAppUserEntityAction({ ...action }),
        loadApplicationsEntityAction({ organizationId: action.organizationId })
      ];
    })
  );
}
