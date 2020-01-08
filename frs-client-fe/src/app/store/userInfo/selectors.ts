import {createFeatureSelector} from "@ngrx/store";
import { UserInfoState } from './reducers';

export const selectUserInfoState = createFeatureSelector<UserInfoState>('userInfo');
