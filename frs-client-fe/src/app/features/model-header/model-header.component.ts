import {ChangeDetectionStrategy, Component, OnDestroy, OnInit} from '@angular/core';
import {Model} from '../../data/model';
import {ModelHeaderFacade} from './model-header.facade';
import {Observable} from 'rxjs';
import {ApplicationHeaderFacade} from '../application-header/application-header.facade';
import {MatDialog} from '@angular/material';

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
  public apiKey: string;

  constructor(private modelHeaderFacade: ModelHeaderFacade, public dialog: MatDialog) {}

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

  rename() {
    // this.modelHeaderFacade.rename(name);
  }

  copy() {
    const input = document.createElement('input');
    input.setAttribute('value', this.apiKey);
    document.body.appendChild(input);
    input.select();
    document.body.removeChild(input);
  }
}
