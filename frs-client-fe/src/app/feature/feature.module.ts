import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoginFormComponent } from './containers/login-form/login-form.component';
import { ToolBarComponent } from './containers/tool-bar/tool-bar.component';
import {CustomMaterialModule} from "../ui/custom-material/custom-material.module";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {RouterModule} from "@angular/router";
import { SignUpFormComponent } from './containers/sign-up-form/sign-up-form.component';
import { AlertComponent } from './containers/alert/alert.component';

@NgModule({
  declarations: [LoginFormComponent, ToolBarComponent, SignUpFormComponent, AlertComponent],
  exports: [
    ToolBarComponent,
    LoginFormComponent,
    SignUpFormComponent,
    AlertComponent
  ],
  imports: [
    CommonModule,
    CustomMaterialModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule
  ]
})
export class FeatureModule { }
