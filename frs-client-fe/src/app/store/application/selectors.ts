import { createSelector, createFeatureSelector } from '@ngrx/store';
import { applicationAdapter, AppEntityState } from './reducers';
import { Application } from 'src/app/data/application';
import { EntityState } from '@ngrx/entity';

export const selectApplicationEntityState = createFeatureSelector<EntityState<Application>>('application');
const { selectEntities, selectIds, selectAll } = applicationAdapter.getSelectors();

export const selectApplicationById = (id: string) => createSelector(selectApplicationEntityState, selectEntities, appsDictionary => appsDictionary[id]);
export const selectApplications = createSelector(selectApplicationEntityState, selectAll);

export const selectCurrentAppId = createSelector(
  selectApplicationEntityState,
  (state: AppEntityState) => state.selectedAppId
);

export const selectCurrentApp = createSelector(
  selectApplicationEntityState,
  selectCurrentAppId,
  (apps, selectedId) => apps.entities ? apps.entities[selectedId] : null
);

export const selectUserRollForSelectedApp = createSelector(
  selectCurrentApp,
  app => app ? app.role : null
);

export const selectIsPendingApplicationList = createSelector(
  selectApplicationEntityState,
  (state: AppEntityState) => state.isPending
);
