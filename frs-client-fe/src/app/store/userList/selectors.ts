import { createFeatureSelector } from "@ngrx/store";
import { AppState } from "../index";
import { UserListState } from './reducers';

export const selectUserListState = createFeatureSelector<AppState, UserListState>('userList');
