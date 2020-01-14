import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApplicationComponent } from './application.component';
import {ApplicationService} from "./application.service";
import {MatButtonModule} from "@angular/material/button";
import {RouterModule} from "@angular/router";
import {AuthGuard} from "../../core/auth/auth.guard";
import {ToolBarModule} from "../../features/tool-bar/tool-bar.module";
import {OrganizationHeaderModule} from "../../features/organization-header/organization-header.module";
import {SpinnerModule} from "../../features/spinner/spinner.module";



@NgModule({
  declarations: [ApplicationComponent],
  imports: [
    CommonModule,
    MatButtonModule,
    SpinnerModule,
    RouterModule.forChild([
      {path: '', component: ApplicationComponent, canActivate: [AuthGuard]},
    ]),
    ToolBarModule,
    OrganizationHeaderModule
  ],
  providers: [ApplicationService]
})
export class ApplicationModule { }
