import { NgModule } from '@angular/core';
import { StoreModule } from '@ngrx/store';
import { modelReducer } from './reducers';

@NgModule({
  declarations: [],
  imports: [
      StoreModule.forFeature('model', modelReducer)
  ]
})
export class ModelStoreModule {}