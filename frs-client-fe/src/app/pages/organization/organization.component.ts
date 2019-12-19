import {ChangeDetectionStrategy, Component, OnDestroy, OnInit} from '@angular/core';
import {createFeatureSelector, Store} from "@ngrx/store";
import {AppState} from "../../store";
import {Observable, Subscription} from "rxjs";
import {ActivatedRoute, Params, Router} from "@angular/router";
import {Organization} from "../../data/organization";
import {ROUTERS_URL} from "../../data/routers-url.variable";
import {OrganizationEnService} from "../../store/organization/organization-entitys.service";
import {
  getSelectedOrganization,
  getSelectOrganizationId,
  OrganizationSelectors
} from "../../store/organization/selectors";
import {SetSelectedId} from "../../store/organization/action";

@Component({
  selector: 'app-organization',
  templateUrl: './organization.component.html',
  styleUrls: ['./organization.component.sass'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OrganizationComponent implements OnInit, OnDestroy {
  organizationSubscription: Subscription;
  selected: string | null;
  getState: Observable<any>;

  organization$: Observable<Organization[]>;

  constructor(
    private store: Store<AppState>,
    private router: Router,
    private route: ActivatedRoute,
    private organizationEnService: OrganizationEnService,
  ) {
    console.log('OrganizationSelectors', OrganizationSelectors);

    this.store.select(getSelectedOrganization).subscribe(e => {
      console.log('getSelectedOrganization', e);
    });

    this.store.select(OrganizationSelectors.selectCollection).subscribe(e => {
      console.log('selectOrganizationEntities', e);
    });



    const selectEntityCache = createFeatureSelector<any>('entityCache');
    this.getState = this.store.select(selectEntityCache);

    this.store.subscribe(e => {
      console.log('store', e);
    });

    this.getState.subscribe(e => {
      console.log('test selector', e);
    });
  }

  ngOnInit() {
    this.organization$ = this.organizationEnService.entities$;
    this.organizationEnService.getAll();
    this.store.select(getSelectOrganizationId).subscribe(e => {
      console.log('getSelectOrganizationId', e);
    });

    this.route.queryParams.subscribe((params: Params) => {
      console.log(params);
    });

    console.log(this.route.snapshot.params);

    this.organizationSubscription = this.organization$.subscribe((data: Array<Organization>) => {
      console.log(data);
      const { id } = this.route.snapshot.params;
      if(data[0] && !id) {
        this.router.navigate([ROUTERS_URL.ORGANIZATION, data[0].id])
      }
      else {
        this.selected = id;
      }
    });
  }

  ngOnDestroy() {
    this.organizationSubscription.unsubscribe();
  }

  addNew() {
    this.organizationEnService.add({
      id: '33333',
      name: 'some new name'
    });

    // OrganizationActions.addOrganization
    // this.store.dispatch(addOrganization({organization: { id: 'id12', name: 'name'}}));
    // setSelectedId({selectId: 'blabla'});
    this.store.dispatch(new SetSelectedId('asdfdasdfdaf222222222222'))
  }

}
