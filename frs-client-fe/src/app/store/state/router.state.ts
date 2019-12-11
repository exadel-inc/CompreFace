import { RouterStateSnapshot } from '@angular/router'
import { RouterStateSerializer } from '@ngrx/router-store'
import {RouterStateUrl} from "../reducers/router";

export class AppSerializer implements RouterStateSerializer<RouterStateUrl > {
  serialize(state: RouterStateSnapshot): RouterStateUrl  {
    let currentRoute = state.root;

    while (currentRoute.firstChild) {
      currentRoute = currentRoute.firstChild
    }

    const {
      url,
      root: { queryParams }
    } = state;
    const { params, data } = currentRoute;

    return { url, params, queryParams, data }
  }
}
