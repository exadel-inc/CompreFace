import { NgModule } from '@angular/core';
import {StoreModule} from "@ngrx/store";
import {UserInfoReducer} from "./reducers";
import {EffectsModule} from "@ngrx/effects";
import {UserInfoEffect} from "./effects";

@NgModule({
  declarations: [],
  imports: [
    EffectsModule.forFeature([UserInfoEffect]),
    StoreModule.forFeature('userInfo', UserInfoReducer),
  ],
})
export class UserInfoStoreModule { }
