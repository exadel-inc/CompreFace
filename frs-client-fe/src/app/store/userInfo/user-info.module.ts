import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {StoreModule} from "@ngrx/store";
import {UserInfoReducer} from "./reducers";
import {EffectsModule} from "@ngrx/effects";
import {UserInfoEffect} from "./effects";

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    EffectsModule.forFeature([UserInfoEffect]),
    StoreModule.forFeature('userInfo', UserInfoReducer),
  ],
})
export class UserInfoStoreModule { }
