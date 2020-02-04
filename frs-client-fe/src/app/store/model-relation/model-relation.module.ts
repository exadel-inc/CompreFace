import { NgModule } from '@angular/core';
import { StoreModule } from '@ngrx/store';
import { modelRelationReducer } from './reducers';

@NgModule({
  declarations: [],
  imports: [
      StoreModule.forFeature('model-relation', modelRelationReducer)
  ]
})
export class ModelRelationStoreModule {}