import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {OrganizationComponent} from "./organization.component";
import {RouterModule} from "@angular/router";
import {AuthGuard} from "../../core/auth/auth.guard";
import {ToolBarModule} from "../../features/tool-bar/tool-bar.module";
import { ApplicationListComponent } from 'src/app/pages/organization/components/application-list/application-list-container.component';
import { MatButtonModule } from '@angular/material/button';



@NgModule({
  declarations: [OrganizationComponent, ApplicationListComponent],
  imports: [
    CommonModule,
    MatButtonModule,
    RouterModule.forChild([
      {path: '', component: OrganizationComponent, canActivate: [AuthGuard]}
    ]),
    ToolBarModule,
  ],
  exports: [RouterModule]
})
export class OrganizationModule { }
