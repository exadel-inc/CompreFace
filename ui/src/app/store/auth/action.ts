/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
import { createAction, props } from '@ngrx/store';

import { SignUp } from '../../data/interfaces/sign-up';
import { ChangePassword } from '../../data/interfaces/change-password';
import { HttpErrorResponse } from '@angular/common/http';

export const logIn = createAction('[Auth] Login', props<{ email: string; password: string }>());
export const logInSuccess = createAction('[Auth] Login Success');
export const logInFail = createAction('[Auth] Login Failure', props<{ error: any }>());

export const refreshToken = createAction('[Refresh Token] App', props<{ grant_type: string; scope: string }>());

export const recoveryPassword = createAction('[Password Recovery]', props<{ email: string }>());
export const recoveryPasswordSuccess = createAction('[Password Recovery] Success');
export const recoveryPasswordFail = createAction('[Password Recovery] Fail', props<{ error: any }>());

export const resetPassword = createAction('[Password Reset]', props<{ password: string; token: string }>());
export const resetPasswordSuccess = createAction('[Password Reset] Success');
export const resetPasswordFail = createAction('[Password Reset] Fail', props<{ error: any }>());

export const signUp = createAction('[Auth] Sign up', props<SignUp>());
export const signUpFail = createAction('[Auth] Sign up Failure', props<{ error: any }>());
export const signUpSuccess = createAction(
  '[Auth] Sign up Success',
  props<{ confirmationNeeded: boolean; email: string; password: string }>()
);

export const logOut = createAction('[Auth] Logout');
export const clearUserToken = createAction('[Auth] Clear User Token');
export const resetErrorMessage = createAction('[Auth] Reset Error Message');

export const changePassword = createAction('[Auth] Change password', props<ChangePassword>());
export const changePasswordFail = createAction('[Auth] Change password Failure', props<{ error: any }>());
export const changePasswordSuccess = createAction('[Auth] Change password Success');

export const confirmEmailMessage = createAction('[Auth] Confirm Email Message');
