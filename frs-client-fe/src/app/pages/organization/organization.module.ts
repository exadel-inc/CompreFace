import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {OrganizationComponent} from "./organization.component";
import {RouterModule} from "@angular/router";
import {AuthGuard} from "../../core/auth/auth.guard";
import {ToolBarModule} from "../../features/tool-bar/tool-bar.module";
import {OrganizationHeaderModule} from "../../features/organization-header/organization-header.module";
import {OrganizationService} from "./organization.service";
import { ApplicationListComponent } from 'src/app/features/application-list/application-list-container.component';
import { MatButtonModule } from '@angular/material/button';
import { TableModule } from 'src/app/features/table/table.module';
import { UserTableModule } from 'src/app/features/user-table/user-table.module';

@NgModule({
  declarations: [OrganizationComponent, ApplicationListComponent],
  imports: [
    TableModule,
    UserTableModule,
    CommonModule,
    MatButtonModule,
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
