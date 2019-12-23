import { Observable, Subscription } from 'rxjs';
import { Component, OnInit, OnDestroy } from '@angular/core';
import { Store } from '@ngrx/store';
import { AppState } from 'src/app/store';
import { selectApplicationListState } from 'src/app/store/applicationList/selectors';
import { ApplicationListState } from 'src/app/store/applicationList/reducers';
import { FetchApplicationList, CreateApplication } from 'src/app/store/applicationList/action';
import { CreateDialogComponent } from 'src/app/features/create-dialog/create-dialog.component';
import { MatDialog } from '@angular/material';


@Component({
  selector: 'application-list-container',
  templateUrl: './application-list-container.component.html',
  styleUrls: ['./application-list-container.component.sass']
})
export class ApplicationListComponent implements OnInit, OnDestroy {
  public isLoading: boolean = true;
  public errorMessage: string;
  public applicationList: any[];
  private applicationListState: Observable<ApplicationListState>;
  private applicationListStateSubscription: Subscription;

  constructor(private store: Store<AppState>, public dialog: MatDialog) {
    this.applicationListState = this.store.select(selectApplicationListState);
    this.store.dispatch(new FetchApplicationList({
      organizationId: '0'
    }));
  }

  ngOnInit() {
    this.applicationListStateSubscription = this.applicationListState.subscribe((state: ApplicationListState) => {
      this.isLoading = state.isLoading;
      this.errorMessage = state.errorMessage;
      this.applicationList = state.applicationList;
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
  }
}
