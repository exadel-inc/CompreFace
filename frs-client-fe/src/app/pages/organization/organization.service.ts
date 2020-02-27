import {Injectable} from '@angular/core';
import {selectCurrentOrganizationId} from '../../store/organization/selectors';
import {combineLatest, merge, Observable, Subscription} from 'rxjs';
import {ROUTERS_URL} from '../../data/routers-url.variable';
import {setSelectedId} from '../../store/organization/action';
import {Organization} from '../../data/organization';
import {ActivatedRoute, Router} from '@angular/router';
import {OrganizationEnService} from '../../store/organization/organization-entitys.service';
import {Store} from '@ngrx/store';
import {AppState} from '../../store';
import {SelectRouterIdParam} from '../../store/router/selectors';
import {filter, map} from 'rxjs/operators';
import {getUserInfo} from '../../store/userInfo/action';

@Injectable()
export class OrganizationService {
  selectedId$: Observable<string>;
  routId$: Observable<string>;
  private setInitialValueFromUrl$: Observable<any>;
  private redirectToOrganization$: Observable<any>;
  private setFirstOrganization$: Observable<any>;
  private setSelectedIdSubscription: Subscription;
  private redirectSubscription: Subscription;
  private organization$: Observable<Array<Organization>>;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private organizationEnService: OrganizationEnService,
    private store: Store<AppState>
  ) {}

  initUrlBindingStreams() {
    this.organizationEnService.load();
    this.store.dispatch(getUserInfo());

    this.organization$ = this.organizationEnService.entities$;
    this.selectedId$ = this.store.select(selectCurrentOrganizationId);
    this.routId$ = this.store.select(SelectRouterIdParam);

    this.setInitialValueFromUrl$ = combineLatest(this.selectedId$, this.organization$, this.routId$).pipe(
      filter(([selectedId, data, routerId]) => {
        return data.length && routerId && selectedId === null;
      }),
      filter(this.isValidId),
      map(([selectedId, data, routerId]) => routerId)
    );

    this.setFirstOrganization$ = combineLatest(this.selectedId$, this.organization$, this.routId$).pipe(
      filter(([selectedId, data, routerId]) => {
        return data.length && routerId && selectedId === null;
      }),
      filter((data) => !this.isValidId(data)),
      map(([selectedId, data, routerId]) => data[0].id)
    );

    this.redirectToOrganization$ = combineLatest(this.selectedId$, this.organization$, this.routId$).pipe(
      filter(([selectedId, data, routerId]) => {
        return !!(data.length && !routerId && selectedId === null);
      }),
      map(([selectedId, data, routerId]) => data[0].id)
    );

    this.setSelectedIdSubscription = this.setInitialValueFromUrl$.subscribe(routerId => {
      this.store.dispatch(setSelectedId({selectId: routerId}));
    });

    this.redirectSubscription = merge(
      this.redirectToOrganization$,
      this.setFirstOrganization$
    ).subscribe(selectedId => {
      this.router.navigate([ROUTERS_URL.ORGANIZATION, selectedId]);
    });
  }

  unSubscribe() {
    this.setSelectedIdSubscription.unsubscribe();
    this.redirectSubscription.unsubscribe();
    // clear selected Id for Organization
    this.store.dispatch(setSelectedId({selectId: null}));
  }

  isValidId([selectedId, data, routerId]) {
    return data.some(array => array.id === routerId);
  }
}
