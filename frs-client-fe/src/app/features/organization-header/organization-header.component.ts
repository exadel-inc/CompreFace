import {ChangeDetectionStrategy, Component, Input, OnInit} from '@angular/core';
import {Router} from "@angular/router";
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
  organizations$: Observable<Organization[]>;
  private selectedId$: Observable<any>;

  constructor(private router: Router, private organizationHeaderService: OrganizationHeaderService) { }

  ngOnInit() {
    this.organizations$ = this.organizationHeaderService.organization$;
    this.selectedId$ = this.organizationHeaderService.selectedId$
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
