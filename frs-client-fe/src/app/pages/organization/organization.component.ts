import {ChangeDetectionStrategy, Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Params, Router} from "@angular/router";
import {OrganizationEnService} from "../../store/organization/organization-entitys.service";
import {Organization} from "../../data/organization";
import {ROUTERS_URL} from "../../data/routers-url.variable";
import {Subscription} from "rxjs";

@Component({
  selector: 'app-organization',
  templateUrl: './organization.component.html',
  styleUrls: ['./organization.component.sass'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OrganizationComponent implements OnInit, OnDestroy {
  selected: string | null;
  private organizationSubscription: Subscription;
  private organization$: any;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private organizationEnService: OrganizationEnService,
  ) {

}

  ngOnInit() {
    this.organizationEnService.getAll();
    // this.store.select(getSelectOrganizationId).subscribe(e => {
    //   console.log('getSelectOrganizationId', e);
    // });
    //
    // this.route.queryParams.subscribe((params: Params) => {
    //   console.log(params);
    // });
    //
    // console.log(this.route.snapshot.params);
    //
    // this.organizationSubscription = this.organization$.subscribe((data: Array<Organization>) => {
    //   console.log(data);
    //   const { id } = this.route.snapshot.params;
    //
    //   if(data[0] && !id) {
    //     this.router.navigate([ROUTERS_URL.ORGANIZATION, data[0].id])
    //   }
    //   else {
    //     this.selected = id;
    //   }
    // });
  }

  ngOnDestroy() {
    // this.organizationSubscription.unsubscribe();
  }

  addNew() {
    // this.organizationEnService.add({
    //   id: '33333',
    //   name: 'some new name'
    // });

    // OrganizationActions.addOrganization
    // this.store.dispatch(addOrganization({organization: { id: 'id12', name: 'name'}}));
    // setSelectedId({selectId: 'blabla'});
    // this.store.dispatch(new SetSelectedId('asdfdasdfdaf222222222222'))
  }

}
