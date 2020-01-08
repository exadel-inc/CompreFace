import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StoreModule } from "@ngrx/store";
import { ApplicationReducer } from "./reducers";

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    StoreModule.forFeature('application', ApplicationReducer)
  ]
})
export class ApplicationStoreModule { }
