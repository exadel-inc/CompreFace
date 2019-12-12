import {routerReducer, RouterReducerState} from "@ngrx/router-store";
import {RouterStateUrl} from "./state/router.state";
import {State} from "./state/auth.state";
import {reducer} from "./reducers/auth";

export interface AppState {
  authState: State;
  router: RouterReducerState<RouterStateUrl>
}

export const reducers = {
  auth: reducer,
  router: routerReducer
};
