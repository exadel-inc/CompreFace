import { NgModule } from '@angular/core';
import { StoreModule } from '@ngrx/store';
import { modelReducer } from './reducers';
import { ModelEffects } from "./effects";
import { EffectsModule } from "@ngrx/effects";

@NgModule({
  declarations: [],
  imports: [
    EffectsModule.forFeature([ModelEffects]),
    StoreModule.forFeature('model', modelReducer)
  ]
})
export class ModelStoreModule {}
