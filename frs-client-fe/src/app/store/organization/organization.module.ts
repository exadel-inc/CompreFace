import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {StoreModule} from "@ngrx/store";
import {OrganizationReducer} from "./reducers";
// import {OrganizationReducer} from "./reducers";

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    StoreModule.forFeature('Organization', OrganizationReducer),
  ]
})
export class OrganizationStoreModule { }
