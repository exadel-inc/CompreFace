import {ChangeDetectionStrategy, Component, Input, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {ROUTERS_URL} from "../../data/routers-url.variable";
import {Organization} from "../../data/organization";

@Component({
  selector: 'app-organization-header',
  templateUrl: './organization-header.component.html',
  styleUrls: ['./organization-header.component.sass'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OrganizationHeaderComponent implements OnInit {
  @Input() organizations: [Organization];
  @Input() selected: string;

  constructor(private router: Router) { }

  ngOnInit() {
  }

  createNew() {
    this.router.navigate([ROUTERS_URL.ORGANIZATION, 'new']);
    console.log('createNew');
  }

}
