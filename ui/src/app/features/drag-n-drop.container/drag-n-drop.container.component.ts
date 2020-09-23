import { Component, OnInit } from '@angular/core';
import { BreadcrumbsFacade } from '../breadcrumbs/breadcrumbs.facade';
import { Model } from 'src/app/data/model';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-drag-n-drop-container',
  template: `
    <app-drag-n-drop
      [model]="model$ | async">
   </app-drag-n-drop>`
})
export class DragNDropContainerComponent implements OnInit {
  model$: Observable<Model>;

  constructor(private breadcrumbsFacade: BreadcrumbsFacade) { }

  ngOnInit() {
    this.model$ = this.breadcrumbsFacade.model$;
  }
}
