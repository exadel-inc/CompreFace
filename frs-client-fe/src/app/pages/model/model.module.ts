import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from "@angular/material/button";
import { RouterModule } from "@angular/router";
import { AuthGuard } from "../../core/auth/auth.guard";
import { ToolBarModule } from "../../features/tool-bar/tool-bar.module";
import { MatCardModule } from '@angular/material/card';
import { BreadcrumbsModule } from "../../features/breadcrumbs/breadcrumbs.module";
import {ModelPageService} from "./model.service";
import {ModelComponent} from "./model.component";



@NgModule({
  declarations: [ModelComponent],
  imports: [
    CommonModule,
    MatButtonModule,
    RouterModule.forChild([
      { path: '', component: ModelComponent, canActivate: [AuthGuard] },
    ]),
    ToolBarModule,
    BreadcrumbsModule,
    MatCardModule
  ],
  providers: [ModelPageService]
})
export class ModelModule { }
