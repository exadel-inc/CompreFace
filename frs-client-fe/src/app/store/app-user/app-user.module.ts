import { NgModule } from '@angular/core';
import { StoreModule } from "@ngrx/store";
import { appUserReducer } from "./reducers";
import {AppUserEffects} from "./effects";
import {EffectsModule} from "@ngrx/effects";

@NgModule({
  declarations: [],
  imports: [
    EffectsModule.forFeature([AppUserEffects]),
    StoreModule.forFeature('app-user', appUserReducer)
  ]
})
export class AppUserStoreModule { }
