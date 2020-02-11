import {Injectable} from '@angular/core';
import {Store} from '@ngrx/store';
import {Observable, Subscription} from 'rxjs';
import {AppState} from '../../store';
import {IFacade} from '../../data/facade/IFacade';
import {
  selectCurrentModel,
  selectCurrentModelId,
  selectUserRollForSelectedModel,
  selectPendingModel
} from '../../store/model/selectors';
import {Model} from '../../data/model';
import {putUpdatedModelEntityAction} from '../../store/model/actions';
import {selectCurrentOrganizationId} from '../../store/organization/selectors';
import {selectCurrentAppId} from '../../store/application/selectors';
import {filter, map} from 'rxjs/operators';

@Injectable()
export class ModelHeaderFacade implements IFacade {
  selectedId$: Observable<string | null>;
  selectedId: string | null;
  loading$: Observable<boolean>;
  public userRole$: Observable<string | null>;
  public model$: Observable<Model>;
  public modelName$: Observable<string>;
  orgId: string;
  appId: string;
  orgIdSub: Subscription;
  appIdSub: Subscription;
  modelIdSub: Subscription;

  constructor(private store: Store<AppState>) {
    this.model$ = this.store.select(selectCurrentModel);
    this.modelName$ = this.model$.pipe(
      filter(model => !!model),
      map(model => model.name),
    );
    this.userRole$ = this.store.select(selectUserRollForSelectedModel);
    this.selectedId$ = this.store.select(selectCurrentModelId);
    this.loading$ = this.store.select(selectPendingModel);
  }

  initSubscriptions() {
    this.orgIdSub = this.store.select(selectCurrentOrganizationId).subscribe(orgId => this.orgId = orgId);
    this.appIdSub = this.store.select(selectCurrentAppId).subscribe(appId => this.appId = appId);
    this.modelIdSub = this.selectedId$.subscribe(selectedId => this.selectedId = selectedId);
  }

  unsubscribe() {
    this.orgIdSub.unsubscribe();
    this.modelIdSub.unsubscribe();
    this.appIdSub.unsubscribe();
  }

  rename(name: string) {
    this.store.dispatch(putUpdatedModelEntityAction({
      name,
      organizationId: this.orgId,
      applicationId: this.appId,
      modelId: this.selectedId
    }));
  }
}
