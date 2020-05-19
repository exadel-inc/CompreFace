import { Injectable } from '@angular/core';
import { Store } from '@ngrx/store';
import { combineLatest, Observable, Subscription } from 'rxjs';
import { IFacade } from 'src/app/data/facade/IFacade';
import { Model } from 'src/app/data/model';
import { AppState } from 'src/app/store';
import { selectCurrentAppId } from 'src/app/store/application/selectors';
import { createModel, deleteModel, loadModels, updateModel } from 'src/app/store/model/actions';
import { selectModels, selectPendingModel } from 'src/app/store/model/selectors';
import { selectCurrentOrganizationId } from 'src/app/store/organization/selectors';

@Injectable()
export class ModelListFacade implements IFacade {
  models$: Observable<Model[]>;
  isLoading$: Observable<boolean>;
  selectedOrganization$: Observable<string>;
  selectedApplication$: Observable<string>;

  private currentArgsAndApplicationSubscription: Subscription;
  selectedOrganizationId: string;
  selectedApplicationId: string;

  constructor(private store: Store<AppState>) {
    this.models$ = store.select(selectModels);
    this.isLoading$ = store.select(selectPendingModel);
    this.selectedOrganization$ = store.select(selectCurrentOrganizationId);
    this.selectedApplication$ = store.select(selectCurrentAppId);
  }

  initSubscriptions(): void {
    this.currentArgsAndApplicationSubscription = combineLatest(
      this.selectedOrganization$,
      this.selectedApplication$
    ).subscribe((ObservableResult) => {
      if (ObservableResult[0] !== null && ObservableResult[1] !== null) {
        this.selectedOrganizationId = ObservableResult[0];
        this.selectedApplicationId = ObservableResult[1];

        this.loadModels();
      }
    });
  }

  loadModels(): void {
    this.store.dispatch(loadModels({
      organizationId: this.selectedOrganizationId,
      applicationId: this.selectedApplicationId
    }));
  }

  createModel(name: string): void {
    this.store.dispatch(createModel({
      organizationId: this.selectedOrganizationId,
      applicationId: this.selectedApplicationId,
      name
    }));
  }

  renameModel(modelId: string, name: string): void {
    this.store.dispatch(updateModel({
      organizationId: this.selectedOrganizationId,
      applicationId: this.selectedApplicationId,
      modelId,
      name,
    }));
  }

  deleteModel(modelId: string): void {
    this.store.dispatch(deleteModel({
      organizationId: this.selectedOrganizationId,
      applicationId: this.selectedApplicationId,
      modelId,
    }));
  }

  unsubscribe(): void {
    this.currentArgsAndApplicationSubscription.unsubscribe();
  }
}
