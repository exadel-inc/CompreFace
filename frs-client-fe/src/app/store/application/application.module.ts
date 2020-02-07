import {NgModule} from '@angular/core';
import {StoreModule} from '@ngrx/store';
import {ApplicationReducer} from './reducers';
import {EffectsModule} from '@ngrx/effects';
import {ApplicationListEffect} from './effects';

@NgModule({
  declarations: [],
  imports: [
    EffectsModule.forFeature([ApplicationListEffect]),
    StoreModule.forFeature('application', ApplicationReducer)
  ]
})
export class ApplicationStoreModule {
}
