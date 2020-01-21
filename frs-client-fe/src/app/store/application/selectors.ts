import { createSelector, createFeatureSelector } from '@ngrx/store';
import {applicationAdapter} from './reducers';
import * as fromApp from './reducers';
import { Application } from 'src/app/data/application';
import { EntityState } from '@ngrx/entity';

export const selectApplicationEntityState = createFeatureSelector<EntityState<Application>>('application');
const { selectEntities, selectIds, selectAll } = applicationAdapter.getSelectors();

export const selectApplicationById = (id: string) => createSelector(selectApplicationEntityState, selectEntities, appsDictionary => appsDictionary[id]);
export const selectApplications = createSelector(selectApplicationEntityState, selectAll);

export const selectCurrentAppId = createSelector(
  selectApplicationEntityState,
  fromApp.getSelectedAppId
);

export const selectCurrentApp = createSelector(
  selectApplicationEntityState,
  selectCurrentAppId,
  (apps, selectedId) => apps.entities ? apps.entities[selectedId] : null
);

export const SelectUserRollForSelectedApp = createSelector(
  selectCurrentApp,
  app => app ? app.role : null
);
