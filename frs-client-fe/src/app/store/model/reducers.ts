import { ActionReducer, createReducer, on } from '@ngrx/store';
import { EntityState, EntityAdapter, createEntityAdapter } from '@ngrx/entity';
import { Model } from 'src/app/data/model';
import { loadModelsEntityAction, addModelsEntityAction, createModelEntityAction } from './actions';

export interface ModelEntityState extends EntityState<Model> {
  isPending: boolean;
}

export const modelAdapter: EntityAdapter<Model> = createEntityAdapter<Model>()

const initialState: ModelEntityState = modelAdapter.getInitialState({
  isPending: false
});

export const modelReducer: ActionReducer<ModelEntityState> = createReducer(
  initialState,
  on(loadModelsEntityAction, (state) => ({
    ...state,
    isPending: true
  })),
  on(addModelsEntityAction, (state, { models }) => {
    const newState = {
      ...state,
      isPending: false
    };

    return modelAdapter.addAll(models, newState);
  }),
  on(createModelEntityAction, (state) => ({
    ...state,
    isPending: true
  }))
);
