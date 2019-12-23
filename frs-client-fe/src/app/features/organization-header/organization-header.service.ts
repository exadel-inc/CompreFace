import { Injectable } from '@angular/core';
import {OrganizationEnService} from "../../store/organization/organization-entitys.service";
import {
  getSelectOrganizationId
} from "../../store/organization/selectors";
import {Store} from "@ngrx/store";
import {Observable, Subscription} from "rxjs";
import {Organization} from "../../data/organization";
import {AppState} from "../../store";
import {SetSelectedId} from "../../store/organization/action";

@Injectable()
export class OrganizationHeaderService {
  organizationSubscription: Subscription;
  selected$: Observable<string | null>;
  selectedId: string | null;
  getState: Observable<any>;
  public organization$: Observable<Organization[]>;

  constructor(private organizationEnService: OrganizationEnService, private store: Store<AppState>) {
    this.organization$ = this.organizationEnService.entities$;
    this.selected$ = this.store.select(getSelectOrganizationId);
    this.selected$.subscribe(id => {
      console.log('getSelectOrganizationId', id);
      this.selectedId = id;
    });
    // console.log('OrganizationSelectors', OrganizationSelectors);
    //
    // this.store.select(getSelectOrganizationId).subscribe(e => {
    //   console.log('getSelectOrganizationId', e);
    // });
    //
    // this.store.select(OrganizationSelectors.selectCollection).subscribe(e => {
    //   console.log('selectOrganizationEntities', e);
    // });
    //
    //
    //
    // const selectEntityCache = createFeatureSelector<any>('entityCache');
    // this.getState = this.store.select(selectEntityCache);
    //
    // this.store.subscribe(e => {
    //   console.log('store', e);
    // });
    //
    // this.getState.subscribe(e => {
    //   console.log('test selector', e);
    // });
  }

  select(id: string) {
    this.store.dispatch(new SetSelectedId({selectId: id}));
  }

  rename(name: string) {
    // console.log(this.selectedId);
    this.organizationEnService.update({name, id: this.selectedId})
  }
}
