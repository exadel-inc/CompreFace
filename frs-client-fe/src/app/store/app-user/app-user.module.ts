import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StoreModule } from "@ngrx/store";
import { appUserReducer } from "./reducers";

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    StoreModule.forFeature('app-user', appUserReducer)
  ]
})
export class AppUserStoreModule { }
