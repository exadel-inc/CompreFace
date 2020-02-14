import {createReducer, on, Action, ActionReducer} from '@ngrx/store';
import {EntityState, EntityAdapter, createEntityAdapter} from '@ngrx/entity';
import {Model} from 'src/app/data/model';
import {
  loadModelsEntityAction,
  addModelsEntityAction,
  createModelEntityAction,
  setSelectedIdModelEntityAction,
  updatedModelEntityAction,
  putUpdatedModelEntityAction
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
  on(loadModelsEntityAction, (state) => ({ ...state, isPending: true })),
  on(addModelsEntityAction, (state, { models }) => modelAdapter.addAll(models, { ...state, isPending: false })),
  on(createModelEntityAction, (state) => ({ ...state, isPending: true })),
  on(setSelectedIdModelEntityAction, (state, { selectedId }) => ({ ...state, selectedId })),
  on(putUpdatedModelEntityAction, (state) => ({ ...state, isPending: true })),
  on(updatedModelEntityAction, (state, { model }) => modelAdapter.updateOne(
    { id: model.id, changes: model },
    { ...state, isPending: false }
  )),
);

export function modelReducer(modelState: ModelEntityState, action: Action) {
  return reducer(modelState, action);
}
