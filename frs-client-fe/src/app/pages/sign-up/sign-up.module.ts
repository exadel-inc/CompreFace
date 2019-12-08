import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {RouterModule} from "@angular/router";
import {FeatureModule} from "../../feature/feature.module";
import {SignUpComponent} from "./sign-up.component";
import {LoginGuard} from "../../core/auth/auth-guard.service";



@NgModule({
  declarations: [SignUpComponent],
  imports: [
    CommonModule,
    RouterModule.forChild([
      {path: '', component: SignUpComponent, canActivate: [LoginGuard]}
    ]),
    FeatureModule
  ]
})
export class SignUpModule { }
