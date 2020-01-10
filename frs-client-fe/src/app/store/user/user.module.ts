import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StoreModule } from "@ngrx/store";
import { AppUserReducer } from "./reducers";

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    StoreModule.forFeature('user', AppUserReducer)
  ]
})
export class UserStoreModule { }
