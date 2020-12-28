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

export const logIn = createAction('[Auth] Login', props<{ email: string; password: string }>());
export const logInSuccess = createAction('[Auth] Login Success');
export const logInFailure = createAction('[Auth] Login Failure', props<{ error: any }>());
export const signUp = createAction('[Auth] Sign up', props<SignUp>());
export const signUpFailure = createAction('[Auth] Sign up Failure', props<{ error: any }>());
export const signUpSuccess = createAction('[Auth] Sign up Success', props<{ confirmationNeeded: boolean }>());
export const logOut = createAction('[Auth] Logout');
export const clearUserToken = createAction('[Auth] Clear User Token');
export const resetErrorMessage = createAction('[Auth] Reset Error Message');
