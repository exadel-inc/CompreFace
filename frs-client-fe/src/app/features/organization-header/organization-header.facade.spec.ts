import {MockStore, provideMockStore} from "@ngrx/store/testing";
import {TestBed} from "@angular/core/testing";
import {OrganizationHeaderFacade} from "./organization-header.facade";
import {MemoizedSelector, Store} from "@ngrx/store";
import {AppState} from "../../store";
import {OrganizationsState} from "../../store/organization/reducers";
import {Router} from "@angular/router";
import {OrganizationEnService} from "../../store/organization/organization-entitys.service";
import {of, Subject} from "rxjs";
import {Organization} from "../../data/organization";
import {getSelectedOrganizationId, SelectUserRollForSelectedOrganization} from "../../store/organization/selectors";
import {ROUTERS_URL} from "../../data/routers-url.variable";


fdescribe('OrganizationHeaderFacade', () => {
  let mockStore: MockStore<AppState>;
  let mockRoleSelector: MemoizedSelector<AppState, OrganizationsState>;
  let mockOrgIdSelector: MemoizedSelector<AppState, string>;
  let facade;

  beforeEach(() => {
      const orgs: Organization[] = [
          {
            "id": "ksdfklsn1111111",
            "name": "Exadel Organization",
            "role": "OWNER"
          },
          {
            "id": "asdfdasdfdaf222222222222",
            "name": "home Organization",
            "role": "ADMIN"
          }
        ]
      ;
      TestBed.configureTestingModule({
        providers: [
          OrganizationHeaderFacade,
          provideMockStore(),
          {
            provide: Router,
            useValue: {
              navigateByUrl: () => {},
              navigate: jasmine.createSpy()
            },
          },
          {
            provide: OrganizationEnService,
            useValue: {
              entities$: of(orgs),
              update: jasmine.createSpy(),
              add: jasmine.createSpy().and.returnValue({subscribe: () => {}})
            }
          }
        ],
      });

      mockStore = TestBed.get(Store);
      mockRoleSelector = mockStore.overrideSelector(SelectUserRollForSelectedOrganization, "ADMIN");
      mockOrgIdSelector = mockStore.overrideSelector(getSelectedOrganizationId, "org_id");
      facade = TestBed.get<OrganizationHeaderFacade>(OrganizationHeaderFacade);
    }
  );
  it('should call select', () => {
    expect(facade.router.navigate).toHaveBeenCalledTimes(0);
    facade.select('org_id');
    expect(facade.router.navigate).toHaveBeenCalledTimes(1);
    expect(facade.router.navigate).toHaveBeenCalledWith([ ROUTERS_URL.ORGANIZATION, 'org_id' ]);
  });

  it('should call update organization', () => {
    expect(facade.organizationEnService.update).toHaveBeenCalledTimes(0);
    facade.rename('new name');
    expect(facade.organizationEnService.update).toHaveBeenCalledTimes(1);
    expect(facade.organizationEnService.update).toHaveBeenCalledWith({ name: 'new name', id: undefined });
  });

  it('should call add organization', () => {
    expect(facade.organizationEnService.add).toHaveBeenCalledTimes(0);
    facade.add({ name: 'new name'});
    expect(facade.organizationEnService.add).toHaveBeenCalledTimes(1);
    expect(facade.organizationEnService.add).toHaveBeenCalledWith({ name: 'new name'});
  });
});
