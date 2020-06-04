import {createReducer, on, Action, ActionReducer} from '@ngrx/store';
import {
  logInSuccess,
  logInFailure,
  signUpSuccess,
  signUpFailure,
  logOut, resetErrorMessage
} from './action';

export interface AuthState {
  errorMessage: string | null;
  successMessage: string | null;
  isLoading: boolean;
}

export const initialState: AuthState = {
  errorMessage: null,
  successMessage: null,
  isLoading: false
};

const reducer: ActionReducer<AuthState> = createReducer(initialState,
  on(logInSuccess, (state) => ({
    ...state,
    errorMessage: null,
    isLoading: false
  })),
  on(logInFailure, (state) => ({
    ...state,
    errorMessage: 'E-mail or Password is incorrect.',
    isLoading: false
  })),
  on(signUpSuccess, (state) => ({
    ...state,
    errorMessage: null,
    successMessage: 'You have created new account, please login into your account',
    isLoading: false
  })),
  on(signUpFailure, (state) => ({
    ...state,
    errorMessage: 'This e-mail is already in use.',
    successMessage: null,
    isLoading: false
  })),
  on(resetErrorMessage, (state) => ({
    ...state,
    errorMessage: null,
  })),
  on(logOut, () => ({ ...initialState }))
);

export function AuthReducer(authState: AuthState, action: Action) {
  return reducer(authState, action);
}
