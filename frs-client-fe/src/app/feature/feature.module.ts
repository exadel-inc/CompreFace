import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoginFormComponent } from './containers/login-form/login-form.component';
import { ToolBarComponent } from './containers/tool-bar/tool-bar.component';
import {CustomMaterialModule} from "../ui/custom-material/custom-material.module";
import {FormsModule} from "@angular/forms";

@NgModule({
  declarations: [LoginFormComponent, ToolBarComponent],
  exports: [
    ToolBarComponent,
    LoginFormComponent
  ],
  imports: [
    CommonModule,
    CustomMaterialModule,
    FormsModule
  ]
})
export class FeatureModule { }
