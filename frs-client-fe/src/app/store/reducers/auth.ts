import { EntityState, EntityAdapter, createEntityAdapter } from '@ngrx/entity';
import {All, AuthActionTypes} from "../actions/auth";
import {initialState} from "../state/auth.state";
import {User} from "../../data/user";

export interface State {
  // is a user authenticated?
  isAuthenticated: boolean;
  // if authenticated, there should be a user object
  user: User | null;
  // error message
  errorMessage: string | null;
}

export const adapter: EntityAdapter<any> = createEntityAdapter<any>();

export function reducer(state = initialState, action: All): State {
  switch (action.type) {

    case AuthActionTypes.LOGIN_SUCCESS: {
      return {
        ...state,
        isAuthenticated: true,
        user: {
          token: action.payload.token,
          email: action.payload.email
        },
        errorMessage: null
      };
    }

    case AuthActionTypes.LOGIN_FAILURE: {
      return {
        ...state,
        errorMessage: 'Incorrect email and/or password.'
      };
    }

    case AuthActionTypes.SIGNUP_SUCCESS: {
      return {
        ...state,
        isAuthenticated: true,
        user: {
          token: action.payload.token,
          email: action.payload.email
        },
        errorMessage: null
      };
    }

    case AuthActionTypes.SIGNUP_FAILURE: {
      return {
        ...state,
        errorMessage: 'That email is already in use.'
      };
    }

    default: {
      return state;
    }
  }
}



