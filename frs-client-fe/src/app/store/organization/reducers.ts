import { createReducer, on } from '@ngrx/store';
import { setSelectedId } from './action';

export interface OrganizationsState {
  selectId: string | null;
}

const initialOrganizationState: OrganizationsState = {
  selectId: null
};

// export function OrganizationReducer(state = initialOrganizationState, action: OrganizationActions): OrganizationsState {
//   switch (action.type) {
//     case OrganizationActionTypes.SET_SELECTED_ID: {
//       return {
//         ...state,
//         selectId: action.payload.selectId
//       };
//     }
//     default: {
//       return state;
//     }
//   }
// }

export const OrganizationReducer = createReducer(initialOrganizationState,
  on(setSelectedId, (state, action) => ({
    ...state,
    selectId: action.selectId
  })));
