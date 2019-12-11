import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoginComponent } from './login.component';
import {RouterModule} from "@angular/router";
import {FeatureModule} from "../../feature/feature.module";
import {LoginGuard} from "../../core/auth/auth.guard";



@NgModule({
  declarations: [LoginComponent],
  imports: [
    CommonModule,
    RouterModule.forChild([
      {path: '', component: LoginComponent, canActivate: [LoginGuard]}
    ]),
    FeatureModule
  ]
})
export class LoginModule { }
