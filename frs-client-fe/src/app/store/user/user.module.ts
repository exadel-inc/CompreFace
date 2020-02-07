import {NgModule} from '@angular/core';
import {StoreModule} from '@ngrx/store';
import {AppUserReducer} from './reducers';
import {EffectsModule} from '@ngrx/effects';
import {UserListEffect} from './effects';

@NgModule({
  declarations: [],
  imports: [
    EffectsModule.forFeature([UserListEffect]),
    StoreModule.forFeature('user', AppUserReducer)
  ]
})
export class UserStoreModule {
}
