import {User} from "../../data/user";
import {AuthActions, AuthActionTypes} from "./action";

export interface State {
  isAuthenticated: boolean;
  user: User | null;
  errorMessage: string | null;
  successMessage: string | null;
  isLoading: boolean;
}

export const initialState: State = {
  isAuthenticated: false,
  user: null,
  errorMessage: null,
  successMessage: null,
  isLoading: false,
};


export function AuthReducer(state = initialState, action: AuthActions): State {
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



