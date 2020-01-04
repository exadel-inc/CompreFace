import { Observable, Subscription } from 'rxjs';
import { Component, OnInit, OnDestroy } from '@angular/core';
import { Store } from '@ngrx/store';
import { selectApplications } from 'src/app/store/application/selectors';
import { selectApplicationListState } from 'src/app/store/applicationList/selectors';
import { ApplicationListState } from 'src/app/store/applicationList/reducers';
import { FetchApplicationList, CreateApplication } from 'src/app/store/applicationList/action';
import { CreateDialogComponent } from 'src/app/features/create-dialog/create-dialog.component';
import { MatDialog } from '@angular/material';
import { Application } from 'src/app/data/application';

@Component({
  selector: 'application-list-container',
  templateUrl: './application-list-container.component.html',
  styleUrls: ['./application-list-container.component.sass']
})
export class ApplicationListComponent implements OnInit, OnDestroy {
  public isLoading: boolean = true;
  public errorMessage: string;
  public applicationList: any[];
  public applications: Observable<Application[]>;
  private applicationListState: Observable<ApplicationListState>;
  private applicationListStateSubscription: Subscription;
  private applicationsSubscription: Subscription;

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

    this.applicationsSubscription = this.applications.subscribe(apps => {
      this.applicationList = apps;
    });
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
    this.applicationsSubscription.unsubscribe();
  }
}
