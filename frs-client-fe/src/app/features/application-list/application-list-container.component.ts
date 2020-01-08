import { Observable, Subscription } from 'rxjs';
import { ChangeDetectionStrategy, Component, OnInit, OnDestroy } from '@angular/core';
import { Store } from '@ngrx/store';
import { selectApplications } from 'src/app/store/application/selectors';
import { selectApplicationListState } from 'src/app/store/applicationList/selectors';
import { ApplicationListState } from 'src/app/store/applicationList/reducers';
import { FetchApplicationList, CreateApplication } from 'src/app/store/applicationList/action';
import { CreateDialogComponent } from 'src/app/features/create-dialog/create-dialog.component';
import { MatDialog } from '@angular/material';
import { Application } from 'src/app/data/application';
import { ITableConfig } from 'src/app/features/table/table.component';

@Component({
  selector: 'application-list-container',
  templateUrl: './application-list-container.component.html',
  styleUrls: ['./application-list-container.component.sass'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ApplicationListComponent implements OnInit, OnDestroy {
  public isLoading: boolean = true;
  public errorMessage: string;
  public applications: Observable<Application[]>;
  public tableConfig: ITableConfig;

  userTableConfig: ITableConfig;

  private applicationListState: Observable<ApplicationListState>;
  private applicationListStateSubscription: Subscription;

  constructor(private store: Store<any>, public dialog: MatDialog) {
    this.applicationListState = this.store.select(selectApplicationListState);
    this.applications = this.store.select(selectApplications);
    this.store.dispatch(new FetchApplicationList({
      organizationId: '0'
    }));
  }

  ngOnInit() {
    this.applicationListStateSubscription = this.applicationListState.subscribe((state: ApplicationListState) => {
      this.isLoading = state.isLoading;
      this.errorMessage = state.errorMessage;
    });

    this.applications.subscribe(apps => {
      this.tableConfig = {
        columns: [{ title: 'Title', property: 'name' }, { title: 'Owner name', property: 'owner' }],
        data: apps.map(app => {
          return { id: app.id, name: app.name, owner: app.owner.firstName };
        })
      };

      this.userTableConfig = {
        columns: [{ title: 'user', property: 'username' }, { title: 'role', property: 'role' }],
        data: [
          {
            "id": 0,
            "firstName": "John",
            "lastName": "Malkovich",
            "accessLevel": "USER"
          },
          {
            "id": 1,
            "firstName": "Tony",
            "lastName": "Stark",
            "accessLevel": "ADMINISTRATOR"
          },
          {
            "id": 2,
            "firstName": "User",
            "lastName": "Role",
            "accessLevel": "USER"
          }
        ]
      }
    });
  }

  public onClick(application): void {
    const dialog = this.dialog.open(CreateDialogComponent, {
      data: {
        entityType: `${application.name} application has been opened`,
        name: ''
      }
    })
  }

  public onCreateNewApp(): void {
    const dialog = this.dialog.open(CreateDialogComponent, {
      data: {
        entityType: 'application',
        name: ''
      }
    });

    dialog.afterClosed().subscribe(res => {
      if (res) {
        this.store.dispatch(new CreateApplication({
          organizationId: '0',
          name: res
        }));
      }
    });
  }

  ngOnDestroy(): void {
    this.applicationListStateSubscription.unsubscribe();
  }
}
