import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {RouterModule} from "@angular/router";
import { AdminComponent } from './admin.component';
import { LoginComponent } from './login/login.component';
import { SingUpComponent } from './sing-up/sing-up.component';

@NgModule({
  declarations: [AdminComponent, LoginComponent, SingUpComponent],
  imports: [
    CommonModule,
    RouterModule.forChild([
      {path: '', component: AdminComponent, children: [
          {path: '', redirectTo: 'login', pathMatch: 'full'},
          { path: 'login', component:  LoginComponent},
          { path: 'sing-up', component:  SingUpComponent},
        ]}
    ])
  ],
  exports: [RouterModule]
})
export class AdminModule { }
