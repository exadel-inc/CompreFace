import {ChangeDetectionStrategy, Component, OnDestroy, OnInit} from '@angular/core';
import {Model} from '../../data/model';
import {ModelHeaderFacade} from './model-header.facade';
import {Observable} from 'rxjs';

@Component({
  selector: 'app-model-header',
  templateUrl: './model-header.component.html',
  styleUrls: ['./model-header.component.sass'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ModelHeaderComponent implements OnInit, OnDestroy {
  public model$: Observable<Model>;
  public userRole$: Observable<string | null>;
  public selectedId$: Observable<any>;
  public loading$: Observable<boolean>;

  constructor(private modelHeaderFacade: ModelHeaderFacade, ) {}

  ngOnInit() {
    this.modelHeaderFacade.initSubscriptions();
    this.model$ = this.modelHeaderFacade.model$;
    this.userRole$ = this.modelHeaderFacade.userRole$;
    this.selectedId$ = this.modelHeaderFacade.selectedId$;
    this.loading$ = this.modelHeaderFacade.loading$;
  }

  ngOnDestroy(): void {
    this.modelHeaderFacade.unsubscribe();
  }

  rename(name) {
    this.modelHeaderFacade.rename(name);
  }
}
