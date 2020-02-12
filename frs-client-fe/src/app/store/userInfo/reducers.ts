import { createReducer, on } from '@ngrx/store';
import {updateUserInfo, resetUserInfo, updateUserAuthorization, getUserInfoSuccess} from './action';
import {User} from '../../data/user';

export interface UserInfoState extends User {
  isAuthenticated: boolean;
}

export const initialState: UserInfoState = {
  isAuthenticated: false,
  username: null,
  guid: null,
  email: null,
  password: null,
  firstName: null,
  lastName: null,
  avatar: null
};

export const UserInfoReducer = createReducer(initialState,
  on(updateUserInfo, (state, action) => ({...state, ...action})),
  on(resetUserInfo, () => ({...initialState})),
  on(updateUserAuthorization, (state, action) => ({...state, isAuthenticated: action.value})),
  on(getUserInfoSuccess, (state, action) => ({...state, ...action.user})));
