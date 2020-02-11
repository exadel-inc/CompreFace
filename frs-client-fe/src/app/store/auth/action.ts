import {createAction, props} from '@ngrx/store';

export const logIn = createAction('[Auth] Login', props<{ username: string, password: string }>());
export const logInSuccess = createAction('[Auth] Login Success');
export const logInFailure = createAction('[Auth] Login Failure');
export const signUp = createAction('[Auth] Sign up', props<{ username: string, password: string, email: string }>());
export const signUpFailure = createAction('[Auth] Sign up Failure');
export const signUpSuccess = createAction('[Auth] Sign up Success');
export const logOut = createAction('[Auth] Logout');
