import {Injectable} from '@angular/core';
import {Store} from '@ngrx/store';
import {AppState} from 'src/app/store';
import {selectModelRelations, selectIsPendingModelrelations} from 'src/app/store/model-relation/selectors';
import {Observable, Subscription, combineLatest, of} from 'rxjs';
import {Application} from 'src/app/data/application';
import {selectCurrentOrganizationId} from 'src/app/store/organization/selectors';
import {selectCurrentAppId} from 'src/app/store/application/selectors';
import {selectCurrentModelId} from 'src/app/store/model/selectors';
import {loadModelRelation, putUpdatedModelRelation} from 'src/app/store/model-relation/actions';
import {IFacade} from 'src/app/data/facade/IFacade';

@Injectable()
export class ModelRelationListFacade implements IFacade {
  public isLoading$: Observable<boolean>;
  public applications$: Observable<Application[]>;
  public selectedOrganizationId: string;
  public selectedAppId: string;
  public selectedModelId: string;
  public availableRoles$: Observable<string[]>;

  private paramsSubscription: Subscription;

  constructor(private store: Store<AppState>) {
    this.isLoading$ = this.store.select(selectIsPendingModelrelations);
    this.applications$ = this.store.select(selectModelRelations);
    this.availableRoles$ = of(['OWNER', 'TRAIN', 'READONLY', 'NONE']);
  }

  public initSubscriptions(): void {
    this.paramsSubscription = combineLatest([
      this.store.select(selectCurrentOrganizationId),
      this.store.select(selectCurrentAppId),
      this.store.select(selectCurrentModelId)
    ]).subscribe(([organizationId, appId, modelId]) => {
      if (organizationId !== null && appId !== null && modelId !== null) {
        this.selectedOrganizationId = organizationId;
        this.selectedAppId = appId;
        this.selectedModelId = modelId;

        this.loadRelations();
      }
    });
  }

  public loadRelations(): void {
    this.store.dispatch(loadModelRelation({
      organizationId: this.selectedOrganizationId,
      applicationId: this.selectedAppId,
      modelId: this.selectedModelId
    }));
  }

  public updateUserRole(id: string, shareMode: string): void {
    this.store.dispatch(putUpdatedModelRelation({
      organizationId: this.selectedOrganizationId,
      applicationId: this.selectedAppId,
      modelId: this.selectedModelId,
      id,
      shareMode
    }));
  }

  public unsubscribe(): void {
    this.paramsSubscription.unsubscribe();
  }
}
