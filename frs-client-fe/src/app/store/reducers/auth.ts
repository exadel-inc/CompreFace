import { EntityState, EntityAdapter, createEntityAdapter } from '@ngrx/entity';
import {All, AuthActionTypes} from "../actions/auth";
import {initialState, State} from "../state/auth.state";

// todo: for users list example
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
        errorMessage: null,
        isLoading: false
      };
    }

    case AuthActionTypes.LOGIN_FAILURE: {
      return {
        ...state,
        errorMessage: 'Incorrect email and/or password.',
        isLoading: false
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
        errorMessage: null,
        successMessage: 'You have created new account, please login into your account',
        isLoading: false
      };
    }

    case AuthActionTypes.SIGNUP_FAILURE: {
      return {
        ...state,
        errorMessage: 'That email is already in use.',
        successMessage: null,
        isLoading: false
      };
    }

    case AuthActionTypes.LOGOUT: {
      // todo: ?
      return initialState;
    }

    default: {
      return state;
    }
  }
}



