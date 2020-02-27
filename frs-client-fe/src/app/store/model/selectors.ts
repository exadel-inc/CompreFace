import {createSelector, createFeatureSelector} from '@ngrx/store';
import {modelAdapter, ModelEntityState} from './reducers';
import {Model} from 'src/app/data/model';
import {EntityState} from '@ngrx/entity';

export const selectModelEntityState = createFeatureSelector<EntityState<Model>>('model');
const { selectEntities, selectAll } = modelAdapter.getSelectors();

export const selectModelById = (id: string) => createSelector(
  selectModelEntityState,
  selectEntities,
  modelsDictionary => modelsDictionary[id]
);
export const selectModels = createSelector(selectModelEntityState, selectAll);

export const selectCurrentModelId = createSelector(
  selectModelEntityState,
  (state: ModelEntityState) => state.selectedId
);

export const selectCurrentModel = createSelector(
  selectModelEntityState,
  selectCurrentModelId,
  (models, selectedId) => models.entities ? models.entities[selectedId] : null
);

export const selectUserRollForSelectedModel = createSelector(
  selectCurrentModel,
  model => model ? model.accessLevel : null
);

export const selectPendingModel = createSelector(
  selectModelEntityState,
  (state: ModelEntityState) => state.isPending
);
