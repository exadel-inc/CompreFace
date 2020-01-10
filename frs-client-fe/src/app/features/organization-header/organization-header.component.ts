import {ChangeDetectionStrategy, Component, Input, OnInit} from '@angular/core';
import {Organization} from "../../data/organization";
import {OrganizationHeaderService} from "./organization-header.service";
import {Observable} from "rxjs";

@Component({
  selector: 'app-organization-header',
  templateUrl: './organization-header.component.html',
  styleUrls: ['./organization-header.component.sass'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OrganizationHeaderComponent implements OnInit {
  public organizations$: Observable<Organization[]>;
  public selected$: Observable<any>;

  constructor(private organizationHeaderService: OrganizationHeaderService) { }

  ngOnInit() {
    this.organizations$ = this.organizationHeaderService.organization$;
    this.selected$ = this.organizationHeaderService.selected$
  }

  createNew() {
    console.log('show popup');
  }

  selectOrganization(id) {
    this.organizationHeaderService.select(id)
  }

  rename(name) {
    this.organizationHeaderService.rename(name)
  }
}
