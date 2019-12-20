import {ChangeDetectionStrategy, Component, Input, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {ROUTERS_URL} from "../../data/routers-url.variable";
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
  private selected$: Observable<string | null>;
  private selectedId: string | null;

  constructor(private router: Router, private organizationHeaderService: OrganizationHeaderService) { }

  ngOnInit() {
    this.organizations$ = this.organizationHeaderService.organization$;
    this.organizationHeaderService.selected$.subscribe(id => this.selectedId = id);
  }

  createNew() {
    // this.router.navigate([ROUTERS_URL.ORGANIZATION, 'new']);
    console.log('show popup');
  }

  selectOrganization(id) {
    this.organizationHeaderService.select(id)
  }

  rename(name) {
    console.log(name);
    this.organizationHeaderService.rename(name)
  }
}
