import {createFeatureSelector, createSelector} from '@ngrx/store';
import {modelRelationEntityAdapter, ModelRelationEntityState} from './reducers';

const { selectAll } = modelRelationEntityAdapter.getSelectors();
const selectModelRelationEntityState = createFeatureSelector<ModelRelationEntityState>('model-relation');

export const selectModelRelations = createSelector(selectModelRelationEntityState, selectAll);
export const selectIsPendingModelrelations = createSelector(selectModelRelationEntityState, state => state.isPending);
