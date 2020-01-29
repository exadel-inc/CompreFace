import { createFeatureSelector, createSelector } from '@ngrx/store';
import { AppState } from '../index';
import { modelRelationEntityAdapter, ModelRelationEntityState } from './reducers';

const { selectAll } = modelRelationEntityAdapter.getSelectors();
const selectModelRelationEntityState = createFeatureSelector<ModelRelationEntityState>('model-relation');

const selectModelRelations = createSelector(selectModelRelationEntityState, selectAll);
const selectIsPendingModelrelations = createSelector(selectModelRelationEntityState, state => state.isPending);
