import {routerReducer, RouterReducerState} from "@ngrx/router-store";
import {AuthReducer, State} from "./auth/reducers";
import {RouterStateUrl} from "./router/reducer";
// import {OrganizationReducer, OrganizationsState} from "./organization/reducers";

export interface AppState {
  authState: State;
  router: RouterReducerState<RouterStateUrl>,
}


// feature reducer need to import into specific module on the page
// this for shared reducers:
export const sharedReducers = {
  auth: AuthReducer,
  router: routerReducer,
};
