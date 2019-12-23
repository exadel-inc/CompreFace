import { Injectable } from '@angular/core';
import {getSelectOrganizationId} from "../../store/organization/selectors";
import {combineLatest, fromEvent, Observable, Subscription} from "rxjs";
import {ROUTERS_URL} from "../../data/routers-url.variable";
import {SetSelectedId} from "../../store/organization/action";
import {Organization} from "../../data/organization";
import {ActivatedRoute, Router} from "@angular/router";
import {OrganizationEnService} from "../../store/organization/organization-entitys.service";
import {Store} from "@ngrx/store";
import {AppState} from "../../store";
import {SelectRouterIdParam, selectRouterState} from "../../store/router/selectors";
import {filter, map} from "rxjs/operators";

@Injectable()
export class OrganizationService {
  selectedId$: Observable<string>;
  private subscription: Subscription;
  private setInitialValueFromUrl$: Observable<any>;
  private redirectToOrganization$: Observable<any>;
  private organization$: Observable<Array<Organization>>;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private organizationEnService: OrganizationEnService,
    private store: Store<AppState>
  ) {
      this.organizationEnService.getAll();
      this.organization$ = this.organizationEnService.entities$;
      this.selectedId$ = this.store.select(getSelectOrganizationId);

      // combineLatest(this.store.select(SelectRouterIdParam), fromEvent(window,'popstate')).subscribe(([id, event]) => {
      //   console.warn("location: " + document.location + ", state: ", event);
      //   // const {id } = this.route.snapshot.params;
      //   console.log(id);
      //   if (id) this.store.dispatch(new SetSelectedId({selectId: id}));
      // });


      this.subscription = combineLatest(this.selectedId$, this.organization$, this.store.select(SelectRouterIdParam))
        .subscribe(( [ selectedId, data, routerId] )  => {
          // console.log(routerParams);
          // const { id: routerId } = routerParams;
          console.log('getSelectOrganizationId', selectedId, data);
          console.log(routerId);
          if(selectedId) {
            if(selectedId !== routerId) {
              // console.warn('do redirect');
              // this.router.navigate([ROUTERS_URL.ORGANIZATION, selectedId ])
            }
          } else {
            if(routerId) {
              // console.warn('selectId', routerId);
              // this.store.dispatch(new SetSelectedId({selectId: routerId}));
            }
          }
        });

    this.setInitialValueFromUrl$ = combineLatest(this.selectedId$, this.organization$, this.store.select(SelectRouterIdParam)).pipe(
      filter(( [ selectedId, data, routerId] ) => {
        return data.length && routerId && selectedId ===null;
      }),
      map(( [ selectedId, data, routerId] ) => routerId)
    );

    this.redirectToOrganization$ = combineLatest(this.selectedId$, this.organization$, this.store.select(SelectRouterIdParam)).pipe(
      filter(( [ selectedId, data, routerId] ) => {
        return !!(data.length && selectedId !== routerId && selectedId);
      }),
      map(( [ selectedId, data, routerId] ) => selectedId)
    );

    this.setInitialValueFromUrl$.subscribe(routerId  => {
      console.warn('selectId', routerId);
      this.store.dispatch(new SetSelectedId({selectId: routerId}));
    });

    this.redirectToOrganization$.subscribe(selectedId => {
      console.warn('do redirect', selectedId);
      this.router.navigate([ROUTERS_URL.ORGANIZATION, selectedId ])
    })
  }
}
