import {createFeatureSelector} from "@ngrx/store";
import {AppState} from "../index";
import { UserInfoState } from './reducers';

export const selectUserInfoState = createFeatureSelector<AppState, UserInfoState>('userInfo');