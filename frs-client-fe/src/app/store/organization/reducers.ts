import { EntityState, EntityAdapter, createEntityAdapter } from '@ngrx/entity';
import {OrganizationActions, OrganizationActionTypes} from "./action";
import {Organization} from "../../data/organization";

export interface State extends EntityState<Organization> {
  selected: number | null
}

export const adapter: EntityAdapter<Organization> = createEntityAdapter<Organization>();

export const initialState: State = adapter.getInitialState({
  selected: null
});

export function OrganizationReducer(state = initialState, action: OrganizationActions): State {
  switch (action.type) {

    case OrganizationActionTypes.LOADED_ALL_SUCCESS: {
      return adapter.addAll(action.payload.organizations, state)
    }

    // case OrganizationActionTypes.LOGIN_FAILURE: {
    //   return {
    //     ...state,
    //     errorMessage: 'Incorrect email and/or password.',
    //     isLoading: false
    //   };
    // }
    //
    // case OrganizationActionTypes.SIGNUP_SUCCESS: {
    //   return {
    //     ...state,
    //     isAuthenticated: true,
    //     user: {
    //       token: action.payload.token,
    //       email: action.payload.email
    //     },
    //     errorMessage: null,
    //     successMessage: 'You have created new account, please login into your account',
    //     isLoading: false
    //   };
    // }
    //
    // case OrganizationActionTypes.SIGNUP_FAILURE: {
    //   return {
    //     ...state,
    //     errorMessage: 'That email is already in use.',
    //     successMessage: null,
    //     isLoading: false
    //   };
    // }

    default: {
      return state;
    }
  }
}



