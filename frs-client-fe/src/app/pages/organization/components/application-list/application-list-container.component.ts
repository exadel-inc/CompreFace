import { Observable } from 'rxjs';
import { Component, OnInit } from '@angular/core';
import { Store } from '@ngrx/store';
import { AppState } from 'src/app/store';
import { selectApplicationListState } from 'src/app/store/applicationList/selectors';
import { ApplicationListState } from 'src/app/store/applicationList/reducers';
import { FetchApplicationList } from 'src/app/store/applicationList/action';

@Component({
  selector: 'application-list-container',
  templateUrl: './application-list-container.component.html',
  styleUrls: ['./application-list-container.component.sass']
})
export class ApplicationListComponent implements OnInit {
  public isLoading: boolean = true;
  public errorMessage: string;
  public applicationList: any[];
  private applicationListState: Observable<ApplicationListState>;

  constructor(private store: Store<AppState>) {
    this.applicationListState = this.store.select(selectApplicationListState);
  }

  ngOnInit() {
    this.applicationListState.subscribe((state: ApplicationListState) => {
      this.isLoading = state.isLoading;
      this.errorMessage = state.errorMessage;
      this.applicationList = state.applicationList;
    });

    this.store.dispatch(new FetchApplicationList());
  }

  public onCreateNewApp(): void {
    alert('create app alert');
  }
}
