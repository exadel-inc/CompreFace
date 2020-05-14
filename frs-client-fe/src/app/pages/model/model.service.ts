import {Injectable} from '@angular/core';
import {Subscription} from 'rxjs';
import {ActivatedRoute, Router} from '@angular/router';
import {Store} from '@ngrx/store';
import {AppState} from '../../store';
import {loadApplications} from '../../store/application/action';
import {ROUTERS_URL} from '../../data/routers-url.variable';
import {filter, take} from 'rxjs/operators';
import {setSelectedIdEntityAction} from '../../store/application/action';
import {getUserInfo} from '../../store/userInfo/action';
import {setSelectedId} from '../../store/organization/action';
import {OrganizationEnService} from '../../store/organization/organization-entitys.service';
import {loadModels, setSelectedIdModel} from '../../store/model/actions';
import {selectModels} from '../../store/model/selectors';

@Injectable()
export class ModelPageService {
  private modelSub: Subscription;
  private appId: string;
  private orgId: string;
  private modelId: string;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private store: Store<AppState>,
    private organizationEnService: OrganizationEnService
  ) { }

  initUrlBindingStreams() {
    this.orgId = this.route.snapshot.queryParams.org;
    this.appId = this.route.snapshot.queryParams.app;
    this.modelId = this.route.snapshot.queryParams.model;

    if (this.appId && this.orgId && this.modelId) {
      this.store.dispatch(setSelectedIdEntityAction({ selectedAppId: this.appId }));
      this.store.dispatch(setSelectedId({ selectId: this.orgId }));
      this.store.dispatch(setSelectedIdModel({ selectedId: this.modelId }));
      this.modelSub = this.store.select(selectModels).pipe(
        filter(model => !model.length),
        take(1)
      ).subscribe(() => {
        this.fetchApps();
      });
    } else {
      this.router.navigate([ROUTERS_URL.ORGANIZATION]);
    }
  }

  unSubscribe() {
    if (this.modelSub) {
      this.modelSub.unsubscribe();
    }
  }

  fetchApps() {
    this.store.dispatch(loadModels({
      organizationId: this.orgId,
      applicationId: this.appId
    }));
    this.store.dispatch(loadApplications({ organizationId: this.orgId }));
    this.store.dispatch(getUserInfo());
    this.organizationEnService.getAll();
  }
}
