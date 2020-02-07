import {NgModule} from '@angular/core';
import {StoreModule} from '@ngrx/store';
import {modelRelationReducer} from './reducers';
import {ModelRelationEffects} from './effects';
import {EffectsModule} from '@ngrx/effects';

@NgModule({
  declarations: [],
  imports: [
    EffectsModule.forFeature([ModelRelationEffects]),
    StoreModule.forFeature('model-relation', modelRelationReducer)
  ]
})
export class ModelRelationStoreModule {}
