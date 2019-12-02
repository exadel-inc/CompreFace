import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {OrganizationComponent} from "./organization.component";
import {RouterModule} from "@angular/router";



@NgModule({
  declarations: [OrganizationComponent],
  imports: [
    CommonModule,
    RouterModule.forChild([
      {path: '', component: OrganizationComponent}
    ])
  ],
  exports: [RouterModule]
})
export class OrganizationModule { }
