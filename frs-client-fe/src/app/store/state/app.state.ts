import * as auth from '../reducers/auth';
// import * as alert from '../reducers/alert';
import {createFeatureSelector} from "@ngrx/store";

export interface AppState {
  authState: auth.State;
  // alertState: alert.State;
}

export const reducers = {
  auth: auth.reducer
};

export const selectAuthState = createFeatureSelector<AppState>('auth');
