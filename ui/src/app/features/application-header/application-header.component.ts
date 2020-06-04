import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material';
import { Observable } from 'rxjs';
import { first } from 'rxjs/operators';

import { Application } from '../../data/application';
import { DeleteDialogComponent } from '../delete-dialog/delete-dialog.component';
import { EditDialogComponent } from '../edit-dialog/edit-dialog.component';
import { ApplicationHeaderFacade } from './application-header.facade';

@Component({
  selector: 'app-application-header',
  templateUrl: './application-header.component.html',
  styleUrls: ['./application-header.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class ApplicationHeaderComponent implements OnInit, OnDestroy {
  app$: Observable<Application>;
  userRole$: Observable<string | null>;
  loading$: Observable<boolean>;

  constructor(private applicationHeaderFacade: ApplicationHeaderFacade, private dialog: MatDialog) { }

  ngOnInit() {
    this.applicationHeaderFacade.initSubscriptions();
    this.app$ = this.applicationHeaderFacade.app$;
    this.userRole$ = this.applicationHeaderFacade.userRole$;
    this.loading$ = this.applicationHeaderFacade.loading$;
  }

  ngOnDestroy(): void {
    this.applicationHeaderFacade.unsubscribe();
  }

  rename(name: string) {
    const dialog = this.dialog.open(EditDialogComponent, {
      width: '300px',
      data: {
        entityType: 'application',
        entityName: name,
      }
    });

    dialog.afterClosed().pipe(first()).subscribe(result => {
      if (result) {
        this.applicationHeaderFacade.rename(result);
      }
    });
  }

  delete(name: string) {
    const dialog = this.dialog.open(DeleteDialogComponent, {
      width: '400px',
      data: {
        entityType: 'application',
        entityName: name,
      }
    });

    dialog.afterClosed().pipe(first()).subscribe(result => {
      if (result) {
        this.applicationHeaderFacade.delete();
      }
    });
  }
}
