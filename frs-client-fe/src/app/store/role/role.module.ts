import { NgModule } from '@angular/core';
import { StoreModule } from '@ngrx/store';
import { RoleReducer } from './reducers';

@NgModule({
  declarations: [],
  imports: [
      StoreModule.forFeature('role', RoleReducer)
  ]
})
export class RoleStoreModule {}
