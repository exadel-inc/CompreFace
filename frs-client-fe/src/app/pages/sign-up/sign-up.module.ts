import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {RouterModule} from "@angular/router";
import {FeatureModule} from "../../feature/feature.module";
import {SignUpComponent} from "./sign-up.component";



@NgModule({
  declarations: [SignUpComponent],
  imports: [
    CommonModule,
    RouterModule.forChild([
      {path: '', component: SignUpComponent}
    ]),
    FeatureModule
  ]
})
export class SignUpModule { }
