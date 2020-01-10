import { Observable, Subscription, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { ChangeDetectionStrategy, Component, OnInit, OnDestroy } from '@angular/core';
import { ApplicationListState } from 'src/app/store/applicationList/reducers';
import { CreateDialogComponent } from 'src/app/features/create-dialog/create-dialog.component';
import { MatDialog } from '@angular/material';
import { ITableConfig } from 'src/app/features/table/table.component';
import { ApplicationListFacade } from './application-list-facade';

@Component({
  selector: 'application-list-container',
  templateUrl: './application-list-container.component.html',
  styleUrls: ['./application-list-container.component.sass'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ApplicationListComponent implements OnInit, OnDestroy {
  public isLoading$: Observable<boolean>;
  public errorMessage: string;
  public tableConfig$: Observable<ITableConfig>;

  private applicationListStateSubscription: Subscription;

  constructor(private applicationFacade: ApplicationListFacade, public dialog: MatDialog) {
    this.applicationFacade.initSubscriptions();
  }

  ngOnInit() {
    this.applicationListStateSubscription = this.applicationFacade.applicationListState$
      .subscribe((state: ApplicationListState) => {
        this.errorMessage = state.errorMessage;
      });

    this.isLoading$ = this.applicationFacade.applicationListState$
      .pipe(map(state => state.isLoading));

    this.tableConfig$ = this.applicationFacade.applications$
      .pipe(
        map(apps => {
          return ({
            columns: [{ title: 'Title', property: 'name' }, { title: 'Owner name', property: 'owner' }],
            data: apps.map(app => ({ id: app.id, name: app.name, owner: app.owner.firstName }))
          })
        })
      );
  }

  public onClick(application): void {
    console.log(`navigate to ${application.name}`);
  }

  public onCreateNewApp(): void {
    const dialog = this.dialog.open(CreateDialogComponent, {
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
    this.applicationListStateSubscription.unsubscribe();
  }
}
