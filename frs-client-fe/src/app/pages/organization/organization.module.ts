import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {OrganizationComponent} from "./organization.component";
import {RouterModule} from "@angular/router";
import {AuthGuard} from "../../core/auth/auth.guard";
import {ToolBarModule} from "../../features/tool-bar/tool-bar.module";
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
      {path: '', component: OrganizationComponent, canActivate: [AuthGuard]}
    ]),
    ToolBarModule,
  ],
  exports: [RouterModule]
})
export class OrganizationModule { }
