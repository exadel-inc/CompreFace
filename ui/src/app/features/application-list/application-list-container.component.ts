import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { CreateDialogComponent } from 'src/app/features/create-dialog/create-dialog.component';
import { ITableConfig } from 'src/app/features/table/table.component';

import { ROUTERS_URL } from '../../data/routers-url.variable';
import { ApplicationListFacade } from './application-list-facade';

@Component({
  selector: 'app-application-list-container',
  templateUrl: './application-list-container.component.html',
  styleUrls: ['./application-list-container.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ApplicationListComponent implements OnInit, OnDestroy {
  isLoading$: Observable<boolean>;
  userRole$: Observable<string>;
  errorMessage: string;
  tableConfig$: Observable<ITableConfig>;

  constructor(private applicationFacade: ApplicationListFacade, private dialog: MatDialog, private router: Router) {
    this.applicationFacade.initSubscriptions();
  }

  ngOnInit() {
    this.isLoading$ = this.applicationFacade.isLoading$;
    this.userRole$ = this.applicationFacade.userRole$;

    this.tableConfig$ = this.applicationFacade.applications$
      .pipe(
        map(apps => {
          return ({
            columns: [{ title: 'Name', property: 'name' }, { title: 'Owner', property: 'owner' }],
            data: apps.map(app => ({ id: app.id, name: app.name, owner: `${app.owner.firstName} ${app.owner.lastName}` }))
          });
        })
      );
  }

  onClick(application): void {
    this.router.navigate([ROUTERS_URL.APPLICATION], {
      queryParams: {
        org: this.applicationFacade.getOrgId(),
        app: application.id
      }
    });
  }

  onCreateNewApp(): void {
    const dialog = this.dialog.open(CreateDialogComponent, {
      width: '300px',
      data: {
        entityType: 'application',
        name: ''
      }
    });

    const dialogSubscription = dialog.afterClosed().subscribe(name => {
      if (name) {
        this.applicationFacade.createApplication(name);
        dialogSubscription.unsubscribe();
      }
    });
  }

  ngOnDestroy(): void {
    this.applicationFacade.unsubscribe();
  }
}
