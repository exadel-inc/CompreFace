import {ChangeDetectionStrategy, Component, OnDestroy, OnInit} from '@angular/core';
import {Application} from '../../data/application';
import {ApplicationHeaderFacade} from './application-header.facade';
import {Observable, Subscription} from 'rxjs';
import {EditDialogComponent} from '../edit-dialog/edit-dialog.component';
import {MatDialog} from '@angular/material';
import {filter, map} from 'rxjs/operators';

@Component({
  selector: 'app-application-header',
  templateUrl: './application-header.component.html',
  styleUrls: ['./application-header.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class ApplicationHeaderComponent implements OnInit, OnDestroy {
  public app$: Observable<Application>;
  public apiKey: string;
  public userRole$: Observable<string | null>;
  public selectedId$: Observable<any>;
  public loading$: Observable<boolean>;
  private apiKeyS: Subscription;

  constructor(private applicationHeaderFacade: ApplicationHeaderFacade, public dialog: MatDialog) {}

  ngOnInit() {
    this.applicationHeaderFacade.initSubscriptions();
    this.app$ = this.applicationHeaderFacade.app$;
    this.userRole$ = this.applicationHeaderFacade.userRole$;
    this.selectedId$ = this.applicationHeaderFacade.selectedId$;
    this.loading$ = this.applicationHeaderFacade.loading$;
    this.apiKeyS = this.app$.pipe(
      filter(app => !!app),
      map(app => app.apiKey),
    ).subscribe(apiKey => this.apiKey = apiKey);
  }

  ngOnDestroy(): void {
    this.applicationHeaderFacade.unsubscribe();
    this.apiKeyS.unsubscribe();
  }

  rename() {
    let currentName = '';
    this.app$.pipe(
      map(app => app.name)
    ).subscribe(name => {
      currentName = name;
    });
    const dialog = this.dialog.open(EditDialogComponent, {
      width: '300px',
      data: {
        entityType: 'application',
        entityName: currentName,
        name: ''
      }
    });

    dialog.afterClosed().subscribe(res => {
      if (res) { this.applicationHeaderFacade.rename(res); }
    });
  }

  copy() {
    const input = document.createElement('input');
    input.setAttribute('value', this.apiKey );
    document.body.appendChild(input);
    input.select();
    document.execCommand('copy');
    document.body.removeChild(input);
  }
}
