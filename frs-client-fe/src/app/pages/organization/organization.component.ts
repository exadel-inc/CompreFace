import {ChangeDetectionStrategy, Component, OnDestroy, OnInit} from '@angular/core';
import {Store} from "@ngrx/store";
import {AppState} from "../../store";
import {Observable, Subscription} from "rxjs";
import {Router} from "@angular/router";
import {EntityCollectionService, EntityServices} from "ngrx-data";
import {Organization} from "../../data/organization";
import {ROUTERS_URL} from "../../data/routers-url.variable";

@Component({
  selector: 'app-organization',
  templateUrl: './organization.component.html',
  styleUrls: ['./organization.component.sass'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OrganizationComponent implements OnInit, OnDestroy {
  organizationSubscription: Subscription;

  organization$: Observable<Organization[]>;
  organizationService: EntityCollectionService<Organization>;

  constructor(private store: Store<AppState>, private router: Router, entityServices: EntityServices) {
    this.organizationService = entityServices.getEntityCollectionService('Organization');
  }

  ngOnInit() {
    this.organization$ = this.organizationService.entities$;
    this.organizationService.getAll();

    this.organizationSubscription = this.organization$.subscribe((data: Array<Organization>) => {
      console.log(data);
      if(data[0]) {
        this.router.navigate([ROUTERS_URL.ORGANIZATION, data[0].id])
      }
    });
  }

  ngOnDestroy() {
    this.organizationSubscription.unsubscribe();
  }

  addNew() {
    this.organizationService.add({
      id: '',
      name: 'some new name'
    })
  }

}
