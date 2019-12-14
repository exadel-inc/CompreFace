import {Component, OnDestroy, OnInit} from '@angular/core';
import {Store} from "@ngrx/store";
import {AppState} from "../../store";
import {selectOrganizationState} from "../../store/organization/selectors";
import {Observable, Subscription} from "rxjs";
import {GetAll} from "../../store/organization/action";

@Component({
  selector: 'app-organization',
  templateUrl: './organization.component.html',
  styleUrls: ['./organization.component.sass']
})
export class OrganizationComponent implements OnInit, OnDestroy {
  getState: Observable<any>;
  stateSubscription: Subscription;

  constructor(private store: Store<AppState>) {
    this.getState = this.store.select(selectOrganizationState);
  }

  ngOnInit() {
    this.stateSubscription = this.getState.subscribe(data => {
      console.log(data);
    });

    this.store.dispatch(new GetAll());
  }

  ngOnDestroy() {
    this.stateSubscription.unsubscribe();
  }

}
