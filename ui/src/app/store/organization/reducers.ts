import {createReducer, on, Action, ActionReducer} from '@ngrx/store';
import {setSelectedId} from './action';

export interface OrganizationsState {
  selectId: string | null;
}

const initialOrganizationState: OrganizationsState = {
  selectId: null
};

const reducer: ActionReducer<OrganizationsState> = createReducer(initialOrganizationState,
  on(setSelectedId, (state, { selectId }) => ({ ...state, selectId }))
);

export function OrganizationReducer(organizationsState: OrganizationsState, action: Action) {
  return reducer(organizationsState, action);
}
