import {createFeatureSelector, createSelector} from '@ngrx/store';
import {UserInfoState} from './reducers';

export const selectUserInfoState = createFeatureSelector<UserInfoState>('userInfo');

export const selectUserId = createSelector(
  selectUserInfoState,
  (userInfo) => userInfo.guid
);
