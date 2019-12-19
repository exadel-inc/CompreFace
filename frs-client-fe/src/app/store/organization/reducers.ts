import {Action, createReducer, on} from "@ngrx/store";
import {ENTITY_CACHE_NAME} from "@ngrx/data";
import {OrganizationActions, OrganizationActionTypes} from "./action";

export interface OrganizationsState {
  selectId: string | null
}

const initialOrganizationState: OrganizationsState = {
  selectId: null
};

// export function OrganizationReducer(state: OrganizationsState = initialOrganizationState, action: Action) {
//   return organizationReducer(state, action);
// }

// const organizationReducer = createReducer(
//   initialOrganizationState,
//   on(OrganizationActions.setSelectedId, (state,   { selectId }) => {
//     console.log(state, selectId);
//     return {
//       ...state,
//       selectId
//     }
//   }),
// );

export function OrganizationReducer(state = initialOrganizationState, action: OrganizationActions): OrganizationsState {
  switch (action.type) {
    case OrganizationActionTypes.SET_SELECTED_ID: {
      console.log(action);
      return {
        ...state,
        selectId: action.payload
      };
    }
    default: {
      return state;
    }
  }
}
// export const getSelectedOrganizationId = (state: OrganizationsState) => state.selectId;


