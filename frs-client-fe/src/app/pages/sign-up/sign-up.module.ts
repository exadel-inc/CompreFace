import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule} from '@angular/router';
import {SignUpComponent} from './sign-up.component';
import {LoginGuard} from '../../core/auth/auth.guard';
import {SignUpFormModule} from '../../features/sign-up-form/sign-up-form.module';

@NgModule({
  declarations: [SignUpComponent],
  imports: [
    CommonModule,
    RouterModule.forChild([
      {path: '', component: SignUpComponent, canActivate: [LoginGuard]}
    ]),
    SignUpFormModule
  ]
})
export class SignUpModule { }
