import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';

import { Application } from '../../data/application';
import { Organization } from '../../data/organization';
import { ROUTERS_URL } from '../../data/routers-url.variable';
import { BreadcrumbsFacade } from './breadcrumbs.facade';

@Component({
  selector: 'app-breadcrumbs',
  templateUrl: './breadcrumbs.component.html',
  styleUrls: ['./breadcrumbs.component.scss']
})
export class BreadcrumbsComponent implements OnInit {
   org$: Observable<Organization>;
   app$: Observable<Application>;
   ROUTERS_URL = ROUTERS_URL;

  constructor(private breadcrumbsFacade: BreadcrumbsFacade, private route: ActivatedRoute) {
    this.org$ = breadcrumbsFacade.org$;
    this.app$ = breadcrumbsFacade.app$;
  }

  ngOnInit() {

  }
}
