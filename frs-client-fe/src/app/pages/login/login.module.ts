import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoginComponent } from './login.component';
import {RouterModule} from "@angular/router";
import {FeatureModule} from "../../feature/feature.module";
import {AuthGuard} from "../../core/auth/auth-guard.service";



@NgModule({
  declarations: [LoginComponent],
  imports: [
    CommonModule,
    RouterModule.forChild([
      {path: '', component: LoginComponent, canActivate: [AuthGuard]}
    ]),
    FeatureModule
  ]
})
export class LoginModule { }
