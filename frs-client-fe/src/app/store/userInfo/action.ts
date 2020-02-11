import {createAction, props} from '@ngrx/store';
import {User} from '../../data/user';

export const updateUserInfo = createAction('[User] Login Success', props<{
  isAuthenticated?: boolean,
  username?: string
}>());
export const resetUserInfo = createAction('[User] Update Token');
export const updateUserAuthorization = createAction('[User] Toggle Authorized', props<{
  value: boolean
}>());
export const getUserInfo = createAction('[User] Get User Info');
export const getUserInfoSuccess = createAction('[User] Get User Info Success', props<{user: User}>());
export const getUserInfoFail = createAction('[User] Get User Info Fail', props<{ errorMessage: string }>());
