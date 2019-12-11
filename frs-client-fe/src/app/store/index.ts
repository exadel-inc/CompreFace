import * as auth from './reducers/auth';
import {createFeatureSelector} from "@ngrx/store";
import {RouterStateUrl} from "./reducers/router";
import {routerReducer, RouterReducerState} from "@ngrx/router-store";

export interface AppState {
  authState: auth.State;
  router: RouterReducerState<RouterStateUrl>
}

export const reducers = {
  auth: auth.reducer,
  router: routerReducer
};
