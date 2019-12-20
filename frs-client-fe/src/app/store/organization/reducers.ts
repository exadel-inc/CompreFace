import {Action, createReducer, on} from "@ngrx/store";
import {ENTITY_CACHE_NAME} from "@ngrx/data";
import {OrganizationActions, OrganizationActionTypes} from "./action";

export interface OrganizationsState {
  selectId: string | null
}

const initialOrganizationState: OrganizationsState = {
  selectId: null
};

export function OrganizationReducer(state = initialOrganizationState, action: OrganizationActions): OrganizationsState {
  switch (action.type) {
    case OrganizationActionTypes.SET_SELECTED_ID: {
      console.log(action);
      return {
        ...state,
        selectId: action.payload.selectId
      };
    }
    default: {
      return state;
    }
  }
}


