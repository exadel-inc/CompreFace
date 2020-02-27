import {ChangeDetectionStrategy, Component, OnDestroy, OnInit} from '@angular/core';
import {Model} from '../../data/model';
import {ModelHeaderFacade} from './model-header.facade';
import {Observable, Subscription} from 'rxjs';
import {MatDialog} from '@angular/material';
import {EditDialogComponent} from '../edit-dialog/edit-dialog.component';
import {filter, map, take, switchMap} from 'rxjs/operators';
import {DeleteDialogComponent} from '../delete-dialog/delete-dialog.component';

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
  public apiKeyS: Subscription;
  public apiKey: string;

  constructor(private modelHeaderFacade: ModelHeaderFacade, public dialog: MatDialog) {}

  public ngOnInit(): void {
    this.modelHeaderFacade.initSubscriptions();
    this.model$ = this.modelHeaderFacade.model$;
    this.userRole$ = this.modelHeaderFacade.userRole$;
    this.selectedId$ = this.modelHeaderFacade.selectedId$;
    this.loading$ = this.modelHeaderFacade.loading$;
    this.apiKeyS = this.model$.pipe(
      filter(app => !!app),
      map(app => app.apiKey),
    ).subscribe(apiKey => this.apiKey = apiKey);
  }

  public ngOnDestroy(): void {
    this.modelHeaderFacade.unsubscribe();
    this.apiKeyS.unsubscribe();
  }

  public rename(): void {
    let currentName = '';
    this.modelHeaderFacade.modelName$.subscribe(name => {
      currentName = name;
    });
    const dialog = this.dialog.open(EditDialogComponent, {
      width: '300px',
      data: {
        entityType: 'model',
        entityName: currentName,
        name: ''
      }
    });

    dialog.afterClosed().subscribe(res => {
      if (res) { this.modelHeaderFacade.rename(res); }
    });
  }

  public delete(): void {
    this.modelHeaderFacade.modelName$
      .pipe(
        take(1),
        switchMap(currentName => {
          return this.dialog.open(DeleteDialogComponent, {
            width: '400px',
            data: {
              entityType: 'Model',
              entityName: currentName
            }
          }).afterClosed();
        })
      )
      .subscribe(res => {
        if (res) { this.modelHeaderFacade.delete(); }
      });
  }

  public copy(): void {
    const input = document.createElement('input');
    input.setAttribute('value', this.apiKey);
    document.body.appendChild(input);
    input.select();
    document.execCommand('copy');
    document.body.removeChild(input);
  }
}
