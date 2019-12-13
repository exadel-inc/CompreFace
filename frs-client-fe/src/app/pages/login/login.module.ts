import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoginComponent } from './login.component';
import {RouterModule} from "@angular/router";
import {LoginGuard} from "../../core/auth/auth.guard";
import {LoginFormModule} from "../../features/login-form/login-form.module";



@NgModule({
  declarations: [LoginComponent],
  imports: [
    CommonModule,
    RouterModule.forChild([
      {path: '', component: LoginComponent, canActivate: [LoginGuard]}
    ]),
    LoginFormModule,
  ]
})
export class LoginModule { }
