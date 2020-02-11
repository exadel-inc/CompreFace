import { createReducer, on } from '@ngrx/store';
import { setSelectedId } from './action';

export interface OrganizationsState {
  selectId: string | null;
}

const initialOrganizationState: OrganizationsState = {
  selectId: null
};

export const OrganizationReducer = createReducer(initialOrganizationState,
  on(setSelectedId, (state, action) => ({...state, selectId: action.selectId})));
