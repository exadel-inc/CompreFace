import { Injectable } from '@angular/core';
import {OrganizationEnService} from "../../store/organization/organization-entitys.service";
import {getSelectedOrganizationId, SelectUserRollForSelectedOrganization} from "../../store/organization/selectors";
import {Store} from "@ngrx/store";
import {Observable, Subscription} from "rxjs";
import {Organization} from "../../data/organization";
import {AppState} from "../../store";
import {SetSelectedId} from "../../store/organization/action";
import {ROUTERS_URL} from "../../data/routers-url.variable";
import {Router} from "@angular/router";
import {IFacade} from "../../core/facade/IFacade";

@Injectable()
export class OrganizationHeaderFacade implements IFacade{
  selectedId$: Observable<string | null>;
  selectedId: string | null;
  public userRole$: Observable<string | null>;
  public organization$: Observable<Organization[]>;
  selectedIdSub: Subscription;

  constructor(private organizationEnService: OrganizationEnService, private store: Store<AppState>, private router: Router) {
    this.organization$ = this.organizationEnService.entities$;
    this.userRole$ = this.store.select(SelectUserRollForSelectedOrganization);
    this.selectedId$ = this.store.select(getSelectedOrganizationId);
  }

  initSubscriptions() {
    this.selectedIdSub = this.selectedId$.subscribe(id => {
      this.selectedId = id;
    });
  }

  unsubscribe() {
    this.selectedIdSub.unsubscribe();
  }

  select(id: string) {
    this.store.dispatch(new SetSelectedId({selectId: id}));
    this.router.navigate([ROUTERS_URL.ORGANIZATION, id ])
  }

  rename(name: string) {
    this.organizationEnService.update({name, id: this.selectedId})
  }

  add(org) {
    this.organizationEnService.add(org).subscribe(org => {
      if(org) {
        this.store.dispatch(new SetSelectedId({selectId: org.id }));
        this.router.navigate([ROUTERS_URL.ORGANIZATION, org.id])
      }
    });
  }
}
