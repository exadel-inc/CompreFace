import { createSelector, createFeatureSelector } from '@ngrx/store';
import { ModelEntityState, modelAdapter } from './reducers';

const { selectAll } = modelAdapter.getSelectors();

export const selectModelState = createFeatureSelector<ModelEntityState>('model');
export const selectModels = createSelector(selectModelState, selectAll);
export const selectIsPending = createSelector(selectModelState, (state) => state.isPending);
export const selectCurrentModelId = createSelector(selectModelState, (state) => state.selectedId);
