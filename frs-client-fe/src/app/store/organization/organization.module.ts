import {NgModule} from '@angular/core';
import {StoreModule} from '@ngrx/store';
import {OrganizationReducer} from './reducers';
import {OrganizationEnService} from './organization-entitys.service';

@NgModule({
  declarations: [],
  imports: [
    StoreModule.forFeature('Organization', OrganizationReducer),
  ],
  providers: [OrganizationEnService]
})
export class OrganizationStoreModule {
}
