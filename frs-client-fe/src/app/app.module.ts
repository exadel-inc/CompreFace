import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { MainLayoutComponent } from './ui/main-layout/main-layout.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {FeatureModule} from "./feature/feature.module";
import { SingUpComponent } from './pages/sing-up/sing-up.component';
import {CustomMaterialModule} from "./ui/custom-material/custom-material.module";

@NgModule({
  declarations: [
    AppComponent,
    MainLayoutComponent,
    SingUpComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    FeatureModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
