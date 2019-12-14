import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {OrganizationComponent} from "./organization.component";
import {RouterModule} from "@angular/router";
import {AuthGuard} from "../../core/auth/auth.guard";
import {ToolBarModule} from "../../features/tool-bar/tool-bar.module";
import {OrganizationStoreModule} from "../../store/organization/organization.module";



@NgModule({
  declarations: [OrganizationComponent],
  imports: [
    CommonModule,
    RouterModule.forChild([
      {path: '', component: OrganizationComponent, canActivate: [AuthGuard]}
    ]),
    ToolBarModule,
    OrganizationStoreModule
  ],
  exports: [RouterModule]
})
export class OrganizationModule { }
