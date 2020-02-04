import { Injectable } from '@angular/core';
import { Store } from "@ngrx/store";
import { Observable, Subscription } from "rxjs";
import { AppState } from "../../store";
import { IFacade } from "../../data/facade/IFacade";
import {
  selectCurrentModel,
  selectCurrentModelId,
  selectUserRollForSelectedModel,
  selectIsPending
} from "../../store/model/selectors";
import { Model } from "../../data/model";
import { putUpdatedModelEntityAction } from "../../store/model/actions";
import { selectCurrentOrganizationId } from "../../store/organization/selectors";

@Injectable()
export class ModelHeaderFacade implements IFacade {
  selectedId$: Observable<string | null>;
  selectedId: string | null;
  loading$: Observable<boolean>;
  public userRole$: Observable<string | null>;
  public model$: Observable<Model>;
  orgId: string;
  orgIdSub: Subscription;
  modelIdSub: Subscription;

  constructor(private store: Store<AppState>) {
    this.model$ = this.store.select(selectCurrentModel);
    this.userRole$ = this.store.select(selectUserRollForSelectedModel);
    this.selectedId$ = this.store.select(selectCurrentModelId);
    this.loading$ = this.store.select(selectIsPending);
  }

  initSubscriptions() {
    this.orgIdSub = this.store.select(selectCurrentOrganizationId).subscribe(orgId => this.orgId = orgId);
    this.modelIdSub = this.selectedId$.subscribe(selectedId => this.selectedId = selectedId);
  }

  unsubscribe() {
    this.orgIdSub.unsubscribe();
    this.modelIdSub.unsubscribe();
  }

  rename(name: string) {
    this.store.dispatch(putUpdatedModelEntityAction({ name, id: this.selectedId, applicationId: this.orgId }));
  }
}
