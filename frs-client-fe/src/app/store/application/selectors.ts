import { createSelector, createFeatureSelector } from '@ngrx/store';
import { applicationAdapter } from './reducers';
import { Application } from 'src/app/data/application';
import { EntityState } from '@ngrx/entity';

export const selectApplicationEntityState = createFeatureSelector<EntityState<Application>>('application');
const { selectEntities, selectIds, selectAll } = applicationAdapter.getSelectors();

export const selectApplicationById = (id: string) => createSelector(selectApplicationEntityState, selectEntities, appsDictionary => appsDictionary[id]);
export const selectApplications = createSelector(selectApplicationEntityState, selectAll);
