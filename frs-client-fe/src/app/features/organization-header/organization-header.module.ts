import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {OrganizationHeaderComponent} from "./organization-header.component";
import {RouterModule} from "@angular/router";
import {MatButtonModule} from "@angular/material/button";
import {EntityTitleModule} from "../entity-title/entity-title.module";
import {OrganizationHeaderService} from "./organization-header.service";

@NgModule({
  declarations: [OrganizationHeaderComponent],
  exports: [
    OrganizationHeaderComponent
  ],
  imports: [
    CommonModule,
    RouterModule,
    MatButtonModule,
    EntityTitleModule
  ],
  providers: [OrganizationHeaderService],
})
export class OrganizationHeaderModule { }
