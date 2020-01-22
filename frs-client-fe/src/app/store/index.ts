import { routerReducer, RouterReducerState } from "@ngrx/router-store";
import { AuthReducer, AuthState } from "./auth/reducers";
import { RouterStateUrl } from "./router/reducer";
import { UserInfoReducer, UserInfoState } from './userInfo/reducers';

export interface AppState {
  authState: AuthState;
  router: RouterReducerState<RouterStateUrl>
  userInfo: UserInfoState
}

// feature reducer need to import into specific module on the page
// this for shared reducers:
export const sharedReducers = {
  auth: AuthReducer,
  router: routerReducer,
  userInfo: UserInfoReducer
};
