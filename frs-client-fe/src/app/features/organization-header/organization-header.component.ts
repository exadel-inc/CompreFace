import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {Organization} from "../../data/organization";
import {OrganizationHeaderFacade} from "./organization-header.facade";
import {Observable} from "rxjs";

@Component({
  selector: 'app-organization-header',
  templateUrl: './organization-header.component.html',
  styleUrls: ['./organization-header.component.sass'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OrganizationHeaderComponent implements OnInit {
  public organizations$: Observable<Organization[]>;
  public userRole$: Observable<string | null>;
  public selectedId$: Observable<any>;

  constructor(private organizationHeaderFacade: OrganizationHeaderFacade) { }

  ngOnInit() {
    this.organizations$ = this.organizationHeaderFacade.organization$;
    this.userRole$ = this.organizationHeaderFacade.userRole$;
    this.selectedId$ = this.organizationHeaderFacade.selectedId$
  }

  createNew() {
    console.log('show popup');
  }

  selectOrganization(id) {
    this.organizationHeaderFacade.select(id)
  }

  rename(name) {
    this.organizationHeaderFacade.rename(name)
  }
}
