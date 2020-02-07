import {Injectable} from '@angular/core';
import {OrganizationEnService} from '../../store/organization/organization-entitys.service';
import {selectCurrentOrganizationId, selectUserRollForSelectedOrganization} from '../../store/organization/selectors';
import {Store} from '@ngrx/store';
import {Observable, Subscription} from 'rxjs';
import {Organization} from '../../data/organization';
import {AppState} from '../../store';
import {SetSelectedId} from '../../store/organization/action';
import {ROUTERS_URL} from '../../data/routers-url.variable';
import {Router} from '@angular/router';
import {IFacade} from '../../data/facade/IFacade';

@Injectable()
export class OrganizationHeaderFacade implements IFacade {
  selectedId$: Observable<string | null>;
  selectedId: string | null;
  public userRole$: Observable<string | null>;
  public organization$: Observable<Organization[]>;
  selectedIdSub: Subscription;

  constructor(private organizationEnService: OrganizationEnService, private store: Store<AppState>, private router: Router) {
    this.organization$ = this.organizationEnService.entities$;
    this.userRole$ = this.store.select(selectUserRollForSelectedOrganization);
    this.selectedId$ = this.store.select(selectCurrentOrganizationId);
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
    this.store.dispatch(new SetSelectedId({ selectId: id }));
    this.router.navigate([ROUTERS_URL.ORGANIZATION, id]);
  }

  rename(name: string) {
    this.organizationEnService.update({name, id: this.selectedId});
  }

  add(org) {
    this.organizationEnService.add(org).subscribe(responce => {
      if (responce) {
        this.store.dispatch(new SetSelectedId({selectId: responce.id}));
        this.router.navigate([ROUTERS_URL.ORGANIZATION, responce.id]);
      }
    });
  }
}
