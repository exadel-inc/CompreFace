import { Injectable } from '@angular/core';
import { IFacade } from 'src/app/core/facade/IFacade';
import { Store } from '@ngrx/store';
import { selectModels, selectIsPending } from 'src/app/store/model/selectors';
import { Observable, combineLatest, Subscription } from 'rxjs';
import { Model } from 'src/app/data/model';
import { AppState } from 'src/app/store';
import { loadModelsEntityAction, createModelEntityAction } from 'src/app/store/model/actions';
import { selectCurrentOrganizationId } from 'src/app/store/organization/selectors';
import { selectCurrentAppId } from 'src/app/store/application/selectors';

@Injectable()
export class ModelListFacade implements IFacade {
  public models$: Observable<Model[]>;
  public isLoading$: Observable<boolean>;
  public selectedOrganization$: Observable<string>;
  public selectedApplication$: Observable<string>;

  private currentArgsAndApplicationSubscription: Subscription;
  private selectedOrganizationId: string;
  private selectedApplicationId: string;

  constructor(private store: Store<AppState>) {
    this.models$ = store.select(selectModels);
    this.isLoading$ = store.select(selectIsPending);
    this.selectedOrganization$ = store.select(selectCurrentOrganizationId);
    this.selectedApplication$ = store.select(selectCurrentAppId);
  }

  public initSubscriptions(): void {
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

  public loadModels(): void {
    this.store.dispatch(loadModelsEntityAction({
      organizationId: this.selectedOrganizationId,
      applicationId: this.selectedApplicationId
    }));
  }

  public createModel(name: string): void {
    this.store.dispatch(createModelEntityAction({
      organizationId: this.selectedOrganizationId,
      applicationId: this.selectedApplicationId,
      name
    }));
  }

  public unsubscribe(): void {
    this.currentArgsAndApplicationSubscription.unsubscribe();
  }
}
