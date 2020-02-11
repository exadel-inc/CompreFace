import {createAction, props} from '@ngrx/store';

// export enum AuthActionTypes {
//   LOGIN = '[Auth] Login',
//   LOGIN_SUCCESS = '[Auth] Login Success',
//   LOGIN_FAILURE = '[Auth] Login Failure',
//   SIGNUP = '[Auth] Sign up',
//   SIGNUP_FAILURE = '[Auth] Sign up Failure',
//   SIGNUP_SUCCESS = '[Auth] Sign up Success',
//   LOGOUT = '[Auth] Logout',
// }

// export class LogIn implements Action {
//   readonly type = AuthActionTypes.LOGIN;
//   constructor(public payload: any) {
//   }
// }

export const logIn = createAction('[Auth] Login', props<{ username: string, password: string }>());

// export class LogInSuccess implements Action {
//   readonly type = AuthActionTypes.LOGIN_SUCCESS;
//   constructor() {}
// }

export const logInSuccess = createAction('[Auth] Login Success');

// export class LogInFailure implements Action {
//   readonly type = AuthActionTypes.LOGIN_FAILURE;
//   constructor(public payload: any) {}
// }

export const logInFailure = createAction('[Auth] Login Failure');

// export class SignUp implements Action {
//   readonly type = AuthActionTypes.SIGNUP;
//   constructor(public payload: any) {}
// }

export const signUp = createAction('[Auth] Sign up', props<{ username: string, password: string, email: string }>());

// export class SignUpFailure implements Action {
//   readonly type = AuthActionTypes.SIGNUP_FAILURE;
//   constructor(public payload: any) {}
// }

export const signUpFailure = createAction('[Auth] Sign up Failure');

// export class SignUpSuccess implements Action {
//   readonly type = AuthActionTypes.SIGNUP_SUCCESS;
//   constructor() {}
// }

export const signUpSuccess = createAction('[Auth] Sign up Success');

// export class LogOut implements Action {
//   readonly type = AuthActionTypes.LOGOUT;
//   constructor() {}
// }

export const logOut = createAction('[Auth] Logout');

// export type AuthActions =
//   | LogIn
//   | LogInSuccess
//   | LogInFailure
//   | SignUp
//   | SignUpSuccess
//   | SignUpFailure
//   | LogOut;
