import { RouterStateSnapshot } from '@angular/router'
import { RouterStateSerializer } from '@ngrx/router-store'

import {Data, Params} from "@angular/router";

export interface RouterStateUrl  {
  url: string
  params: Params
  queryParams: Params
  data: Data
}


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
