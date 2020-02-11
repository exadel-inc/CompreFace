import { createReducer, on } from '@ngrx/store';
import {
  logInSuccess,
  logInFailure,
  signUpSuccess,
  signUpFailure,
  logOut
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

export const AuthReducer = createReducer(initialState,
  on(logInSuccess, (state) => ({
    ...state,
    errorMessage: null,
    isLoading: false
  })),
  on(logInFailure, (state) => ({
    ...state,
    errorMessage: 'Incorrect email and/or password.',
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
        errorMessage: 'That email is already in use.',
        successMessage: null,
        isLoading: false
  })),
  on(logOut, () => ({...initialState}))
);

