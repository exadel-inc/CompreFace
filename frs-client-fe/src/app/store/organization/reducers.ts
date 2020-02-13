import {createReducer, on, Action} from '@ngrx/store';
import {setSelectedId} from './action';

export interface OrganizationsState {
  selectId: string | null;
}

const initialOrganizationState: OrganizationsState = {
  selectId: null
};

export function OrganizationReducer(organizationsState: OrganizationsState, action: Action) {
  return createReducer(initialOrganizationState,
    on(setSelectedId, (state, { selectId }) => ({ ...state, selectId }))
  )(organizationsState, action);
}
