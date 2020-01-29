import { ActionReducer, createReducer, on } from '@ngrx/store';
import { EntityState, EntityAdapter, createEntityAdapter } from '@ngrx/entity';
import { Application } from 'src/app/data/application';
import { loadModelRelation, addModelRelation, putUpdatedModelRelation, updateModelRelation } from './actions';

export interface ModelRelationEntityState extends EntityState<Application> {
  isPending: boolean;
}

export const modelRelationEntityAdapter: EntityAdapter<Application> = createEntityAdapter<Application>();

const initalState = modelRelationEntityAdapter.getInitialState({
  isPending: false
});


export const modelRelationReducer: ActionReducer<ModelRelationEntityState> = createReducer(
  initalState,
  on(loadModelRelation, (state) => ({
    ...state,
    isPending: true
  })),
  on(addModelRelation, (state, { applications }) => {
    const newState = { ...state, isPending: false };
    return modelRelationEntityAdapter.addAll(applications, newState);
  }),
  on(putUpdatedModelRelation, (state) => ({
    ...state,
    isPending: true
  })),
  on(updateModelRelation, (state, { application }) => {
    const newState = { ...state, isPending: false };
    return modelRelationEntityAdapter.updateOne({
      id: application.id,
      changes: {
        role: application.role
      }
    }, newState);
  })
);
