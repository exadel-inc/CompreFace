import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {OrganizationComponent} from "./organization.component";
import {RouterModule} from "@angular/router";
import {AuthGuard} from "../../core/auth/auth.guard";
import {ToolBarModule} from "../../features/tool-bar/tool-bar.module";
import {OrganizationHeaderModule} from "../../features/organization-header/organization-header.module";
import {OrganizationService} from "./organization.service";

@NgModule({
  declarations: [OrganizationComponent],
  imports: [
    CommonModule,
    RouterModule.forChild([
      {path: '', component: OrganizationComponent, canActivate: [AuthGuard]},
      {path: ':id', component: OrganizationComponent, canActivate: [AuthGuard]}
    ]),
    ToolBarModule,
    OrganizationHeaderModule
  ],
  providers: [OrganizationService],
  exports: [RouterModule]
})
export class OrganizationModule { }
