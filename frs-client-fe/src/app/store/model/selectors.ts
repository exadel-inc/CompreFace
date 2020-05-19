import { EntityState } from '@ngrx/entity';
import { createFeatureSelector, createSelector } from '@ngrx/store';
import { Model } from 'src/app/data/model';

import { modelAdapter, ModelEntityState } from './reducers';

export const selectModelEntityState = createFeatureSelector<EntityState<Model>>('model');
const { selectEntities, selectAll } = modelAdapter.getSelectors();

export const selectModels = createSelector(selectModelEntityState, selectAll);

export const selectPendingModel = createSelector(
  selectModelEntityState,
  (state: ModelEntityState) => state.isPending
);
