import {Injectable} from '@angular/core';
import {Observable, Subscription} from "rxjs";
import {ActivatedRoute, Router} from "@angular/router";
import {Store} from "@ngrx/store";
import {AppState} from "../../store";
import {selectApplications} from "../../store/application/selectors";
import {Application} from "../../data/application";
import {FetchApplicationList} from "../../store/applicationList/action";
import {ROUTERS_URL} from "../../data/routers-url.variable";
import {OrganizationEnService} from "../../store/organization/organization-entitys.service";
import {filter, map} from "rxjs/operators";
import {isLoading} from "../../store/applicationList/selectors";

@Injectable()
export class ApplicationService {
  private appsSub: Subscription;
  private apps$: Observable<Array<Application>>;
  private appId: string;
  private orgId: string;
  public app$: Observable<Application>;
  public isLoading$: Observable<boolean>;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private store: Store<AppState>,
    private organizationEnService: OrganizationEnService,
  ) {}

  initUrlBindingStreams() {
    this.orgId = this.route.snapshot.queryParams.org;
    this.appId = this.route.snapshot.queryParams.app;

    if (this.appId && this.orgId) {
      this.apps$ = this.store.select(selectApplications);
      this.isLoading$ = this.store.select(isLoading);

      this.app$ = this.apps$.pipe(
        filter(apps => !!apps.length),
        map(apps => {return apps.find(app => app.id === this.appId) })
      );

      this.apps$.pipe(
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
    this.organizationEnService.getAll();
    this.organizationEnService.entities$.pipe(
      filter(org => !!org.length)
    ).subscribe(() => {
      this.store.dispatch(
        new FetchApplicationList({organizationId: this.orgId})
      )}
    )
  }
}
