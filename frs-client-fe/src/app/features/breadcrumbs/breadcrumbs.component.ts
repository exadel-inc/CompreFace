import {Component, OnInit} from '@angular/core';
import {BreadcrumbsFacade} from './breadcrumbs.facade';
import {Observable} from 'rxjs';
import {Organization} from '../../data/organization';
import {ROUTERS_URL} from '../../data/routers-url.variable';
import {Application} from '../../data/application';
import {ActivatedRoute} from '@angular/router';
import {filter, map} from 'rxjs/operators';
import {Model} from '../../data/model';

@Component({
  selector: 'app-breadcrumbs',
  templateUrl: './breadcrumbs.component.html',
  styleUrls: ['./breadcrumbs.component.sass']
})
export class BreadcrumbsComponent implements OnInit {
  public org$: Observable<Organization>;
  public app$: Observable<Application>;
  public model$: Observable<Model>;
  public routeModel$: Observable<string>;
  public ROUTERS_URL = ROUTERS_URL;

  constructor(private breadcrumbsFacade: BreadcrumbsFacade, private route: ActivatedRoute) {
    this.org$ = breadcrumbsFacade.org$;
    this.app$ = breadcrumbsFacade.app$;
    this.model$ = breadcrumbsFacade.model$;

    this.routeModel$ = route.data.pipe(
      map(data => data.routeName),
      filter(routeName => routeName === 'model')
    );
  }

  ngOnInit() {

  }
}
