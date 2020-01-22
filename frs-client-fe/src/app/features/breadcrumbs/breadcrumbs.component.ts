import { Component, OnInit } from '@angular/core';
import {BreadcrumbsFacade} from "./breadcrumbs.facade";
import {Observable} from "rxjs";
import {Organization} from "../../data/organization";
import { ROUTERS_URL } from "../../data/routers-url.variable";

@Component({
  selector: 'app-breadcrumbs',
  templateUrl: './breadcrumbs.component.html',
  styleUrls: ['./breadcrumbs.component.sass']
})
export class BreadcrumbsComponent implements OnInit {
  public org$: Observable<Organization>;
  public app$: Observable<Organization>;
  ROUTERS_URL = ROUTERS_URL;

  constructor(private breadcrumbsFacade: BreadcrumbsFacade) {
    this.org$ = breadcrumbsFacade.org$;
    this.app$ = breadcrumbsFacade.app$;
  }
  ngOnInit() {}
}
