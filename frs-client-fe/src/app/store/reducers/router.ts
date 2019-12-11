import {Data, Params} from "@angular/router";

export interface RouterStateUrl  {
  url: string
  params: Params
  queryParams: Params
  data: Data
}

// export const getRouterState = createFeatureSelector<fromRouter.RouterReducerState<RouterStateTitle>>('router');
