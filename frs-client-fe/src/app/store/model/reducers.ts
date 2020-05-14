import { createEntityAdapter, EntityAdapter, EntityState } from '@ngrx/entity';
import { Action, ActionReducer, createReducer, on } from '@ngrx/store';
import { Model } from 'src/app/data/model';

import {
  createModel,
  createModelFail,
  deleteModel,
  deleteModelFail,
  loadModels,
  loadModelsFail,
  loadModelsSuccess,
  setSelectedIdModel,
  updateModel,
  updateModelFail,
  updateModelSuccess,
  createModelSuccess,
} from './actions';

export interface ModelEntityState extends EntityState<Model> {
  isPending: boolean;
  selectedId: string;
}

export const modelAdapter: EntityAdapter<Model> = createEntityAdapter<Model>();

const initialState: ModelEntityState = modelAdapter.getInitialState({
  isPending: false,
  selectedId: null
});

const reducer: ActionReducer<ModelEntityState> = createReducer(
  initialState,
  on(loadModels, createModel, updateModel, deleteModel, (state) => ({ ...state, isPending: true })),
  on(loadModelsFail, createModelFail, updateModelFail, deleteModelFail, (state) => ({ ...state, isPending: false })),
  on(loadModelsSuccess, (state, { models }) => modelAdapter.addAll(models, { ...state, isPending: false })),
  on(createModelSuccess, (state, { model }) => modelAdapter.addOne(model, { ...state, isPending: false })),
  on(setSelectedIdModel, (state, { selectedId }) => ({ ...state, selectedId })),
  on(updateModelSuccess, (state, { model }) => modelAdapter.updateOne(
    { id: model.id, changes: model },
    { ...state, isPending: false }
  )),
);

export function modelReducer(modelState: ModelEntityState, action: Action) {
  return reducer(modelState, action);
}
