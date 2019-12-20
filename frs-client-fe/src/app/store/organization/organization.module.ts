import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {StoreModule} from "@ngrx/store";
import {OrganizationReducer} from "./reducers";
import {OrganizationEnService} from "./organization-entitys.service";
// import {OrganizationReducer} from "./reducers";

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    StoreModule.forFeature('Organization', OrganizationReducer),
  ],
  providers: [OrganizationEnService]
})
export class OrganizationStoreModule { }
