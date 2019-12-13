import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {StoreModule} from "@ngrx/store";
import {OrganizationReducer} from "./reducers";
import {EffectsModule} from "@ngrx/effects";
import {OrganizationEffects} from "./effects";


@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    StoreModule.forFeature('organization', OrganizationReducer),
    EffectsModule.forFeature([OrganizationEffects])
  ]
})
export class OrganizationStoreModule { }
