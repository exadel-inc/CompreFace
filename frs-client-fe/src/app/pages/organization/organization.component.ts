import {ChangeDetectionStrategy, Component, OnDestroy, OnInit} from '@angular/core';
import {Store} from "@ngrx/store";
import {AppState} from "../../store";
import {Observable, Subscription} from "rxjs";
import {ActivatedRoute, Params, Router} from "@angular/router";
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
  selected: string | null;

  organization$: Observable<Organization[]>;
  organizationService: EntityCollectionService<Organization>;

  constructor(private store: Store<AppState>, private router: Router, private route: ActivatedRoute, entityServices: EntityServices) {
    this.organizationService = entityServices.getEntityCollectionService('Organization');
  }

  ngOnInit() {
    this.organization$ = this.organizationService.entities$;
    this.organizationService.getAll();

    this.route.queryParams.subscribe((params: Params) => {
      console.log(params);
    });


    console.log(this.route.snapshot.params);

    this.organizationSubscription = this.organization$.subscribe((data: Array<Organization>) => {
      console.log(data);
      const { id } = this.route.snapshot.params;
      if(data[0] && !id) {
        this.router.navigate([ROUTERS_URL.ORGANIZATION, data[0].id])
      }
      else {
        this.selected = id;
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
