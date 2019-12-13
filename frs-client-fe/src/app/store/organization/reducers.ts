import { EntityState, EntityAdapter, createEntityAdapter } from '@ngrx/entity';
import {OrganizationActions, OrganizationActionTypes} from "./action";
import {Organization} from "../../data/organization";

// todo: for users list example
export const adapter: EntityAdapter<any> = createEntityAdapter<any>();

export interface State extends Array<Organization>{}

export const initialState: [Organization?] = [];


export function OrganizationReducer(state = initialState, action: OrganizationActions): State {
  switch (action.type) {

    case OrganizationActionTypes.GET_ALL_SUCCESS: {
      return action.payload;
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



