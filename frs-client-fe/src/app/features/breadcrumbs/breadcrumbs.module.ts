import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {RouterModule} from "@angular/router";
import {BreadcrumbsComponent} from "./breadcrumbs.component";
import {BreadcrumbsFacade} from "./breadcrumbs.facade";

@NgModule({
  declarations: [BreadcrumbsComponent],
  exports: [
    BreadcrumbsComponent
  ],
  imports: [
    CommonModule,
    RouterModule,
  ],
  providers: [BreadcrumbsFacade],
})
export class BreadcrumbsModule { }
