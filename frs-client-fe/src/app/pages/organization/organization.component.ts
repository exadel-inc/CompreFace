import {Component, OnDestroy, OnInit} from '@angular/core';
import {Store} from "@ngrx/store";
import {AppState} from "../../store";
import {selectOrganizationState} from "../../store/organization/selectors";
import {Observable, Subscription} from "rxjs";
import {LoadAll} from "../../store/organization/action";
import {Router} from "@angular/router";

@Component({
  selector: 'app-organization',
  templateUrl: './organization.component.html',
  styleUrls: ['./organization.component.sass']
})
export class OrganizationComponent implements OnInit, OnDestroy {
  getState: Observable<any>;
  stateSubscription: Subscription;

  constructor(private store: Store<AppState>, private Router: Router) {
    this.getState = this.store.select(selectOrganizationState);
  }

  ngOnInit() {
    this.stateSubscription = this.getState.subscribe(organization => {
      console.log(data);
      this.Router.navigate(['organization', '0'] )
    });

    this.store.dispatch(new LoadAll());
  }

  ngOnDestroy() {
    this.stateSubscription.unsubscribe();
  }

}
