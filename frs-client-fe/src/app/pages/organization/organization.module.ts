import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {OrganizationComponent} from "./organization.component";
import {RouterModule} from "@angular/router";
import {AuthGuard} from "../../core/auth/auth-guard.service";



@NgModule({
  declarations: [OrganizationComponent],
  imports: [
    CommonModule,
    RouterModule.forChild([
      {path: '', component: OrganizationComponent, canActivate: [AuthGuard]}
    ])
  ],
  exports: [RouterModule]
})
export class OrganizationModule { }
