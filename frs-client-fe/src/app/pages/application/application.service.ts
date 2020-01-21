import {Injectable} from '@angular/core';
import {Subscription} from "rxjs";
import {ActivatedRoute, Router} from "@angular/router";
import {Store} from "@ngrx/store";
import {AppState} from "../../store";
import {selectApplications} from "../../store/application/selectors";
// import {FetchApplicationList} from "../../store/applicationList12/action";
import {ROUTERS_URL} from "../../data/routers-url.variable";
import {filter} from "rxjs/operators";
import { setSelectedIdEntityAction } from "../../store/application/action";
import {GetUserInfo} from "../../store/userInfo/action";
import {SetSelectedId} from "../../store/organization/action";

@Injectable()
export class ApplicationService {
  private appsSub: Subscription;
  private appId: string;
  private orgId: string;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private store: Store<AppState>,
  ) {}

  initUrlBindingStreams() {
    this.orgId = this.route.snapshot.queryParams.org;
    this.appId = this.route.snapshot.queryParams.app;

    if (this.appId && this.orgId) {
      this.store.dispatch(setSelectedIdEntityAction({ selectedAppId: this.appId}));
      this.store.dispatch(new SetSelectedId({ selectId: this.orgId}));
      this.appsSub = this.store.select(selectApplications).pipe(
        filter(apps => !apps.length)
      ).subscribe(() => {
        this.fetchApps();
      })
    } else {
      this.router.navigate([ROUTERS_URL.ORGANIZATION]);
    }
  }

  unSubscribe() {
    if (this.appsSub) this.appsSub.unsubscribe();
  }

  fetchApps() {
    // this.store.dispatch(new FetchApplicationList({organizationId: this.orgId}));
    this.store.dispatch(new GetUserInfo());
  }
}
