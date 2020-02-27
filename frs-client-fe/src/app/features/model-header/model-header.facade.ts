import {Injectable} from '@angular/core';
import {Store} from '@ngrx/store';
import {Observable, Subject} from 'rxjs';
import {AppState} from '../../store';
import {IFacade} from '../../data/facade/IFacade';
import {
  selectCurrentModel,
  selectCurrentModelId,
  selectUserRollForSelectedModel,
  selectPendingModel
} from '../../store/model/selectors';
import {Model} from '../../data/model';
import {putUpdatedModelEntityAction, deletedModelEntityAction} from '../../store/model/actions';
import {selectCurrentOrganizationId} from '../../store/organization/selectors';
import {selectCurrentAppId} from '../../store/application/selectors';
import {filter, map, takeUntil} from 'rxjs/operators';
import {Router} from '@angular/router';
import {ROUTERS_URL} from 'src/app/data/routers-url.variable';

@Injectable()
export class ModelHeaderFacade implements IFacade {
  public selectedId$: Observable<string | null>;
  public selectedId: string | null;
  public loading$: Observable<boolean>;
  public userRole$: Observable<string | null>;
  public model$: Observable<Model>;
  public modelName$: Observable<string>;
  public orgId: string;
  public appId: string;
  private unsubscribeSubject: Subject<void> = new Subject();

  constructor(private store: Store<AppState>, private router: Router) {
    this.model$ = this.store.select(selectCurrentModel);
    this.modelName$ = this.model$.pipe(
      filter(model => !!model),
      map(model => model.name),
    );
    this.userRole$ = this.store.select(selectUserRollForSelectedModel);
    this.selectedId$ = this.store.select(selectCurrentModelId);
    this.loading$ = this.store.select(selectPendingModel);
  }

  public initSubscriptions(): void {
    this.store.select(selectCurrentOrganizationId).pipe(takeUntil(this.unsubscribeSubject)).subscribe(orgId => this.orgId = orgId);
    this.store.select(selectCurrentAppId).pipe(takeUntil(this.unsubscribeSubject)).subscribe(appId => this.appId = appId);
    this.selectedId$.pipe(takeUntil(this.unsubscribeSubject)).subscribe(selectedId => this.selectedId = selectedId);
  }

  public unsubscribe(): void {
    this.unsubscribeSubject.next();
  }

  public rename(name: string): void {
    this.store.dispatch(putUpdatedModelEntityAction({
      name,
      organizationId: this.orgId,
      applicationId: this.appId,
      modelId: this.selectedId
    }));
  }

  public delete(): void {
    this.selectedId$.pipe(
      takeUntil(this.unsubscribeSubject),
      filter(id => id === null)
    ).subscribe(
      () => this.router.navigate([ROUTERS_URL.APPLICATION], {queryParams: {org: this.orgId, app: this.appId}})
    );

    this.store.dispatch(deletedModelEntityAction({
      organizationId: this.orgId,
      applicationId: this.appId,
      modelId: this.selectedId
    }));
  }
}
