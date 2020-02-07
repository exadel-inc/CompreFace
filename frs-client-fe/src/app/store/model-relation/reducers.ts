import {ActionReducer, createReducer, on} from '@ngrx/store';
import {EntityState, EntityAdapter, createEntityAdapter} from '@ngrx/entity';
import {ModelRelation} from 'src/app/data/modelRelation';
import {loadModelRelation, addModelRelation, putUpdatedModelRelation, updateModelRelation} from './actions';

export interface ModelRelationEntityState extends EntityState<ModelRelation> {
  isPending: boolean;
}

export const modelRelationEntityAdapter: EntityAdapter<ModelRelation> = createEntityAdapter<ModelRelation>();

const initialState = modelRelationEntityAdapter.getInitialState({
  isPending: false
});

export const modelRelationReducer: ActionReducer<ModelRelationEntityState> = createReducer(
  initialState,
  on(loadModelRelation, (state) => ({...state, isPending: true})),
  on(addModelRelation, (state, { applications }) => {
    return modelRelationEntityAdapter.addAll(applications, {...state, isPending: false});
  }),
  on(putUpdatedModelRelation, (state) => ({...state, isPending: true})),
  on(updateModelRelation, (state, { application }) => {
    return modelRelationEntityAdapter.updateOne({
      id: application.id,
      changes: {
        role: application.role
      }
    }, {...state, isPending: false});
  })
);
