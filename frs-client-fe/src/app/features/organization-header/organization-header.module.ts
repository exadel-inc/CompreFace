import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {OrganizationHeaderComponent} from './organization-header.component';
import {RouterModule} from '@angular/router';
import {MatButtonModule} from '@angular/material/button';
import {EntityTitleModule} from '../entity-title/entity-title.module';
import {OrganizationHeaderFacade} from './organization-header.facade';
import {MatCardModule} from '@angular/material';

@NgModule({
  declarations: [OrganizationHeaderComponent],
  exports: [
    OrganizationHeaderComponent
  ],
  imports: [
    CommonModule,
    RouterModule,
    MatButtonModule,
    MatCardModule,
    EntityTitleModule
  ],
  providers: [OrganizationHeaderFacade],
})
export class OrganizationHeaderModule {
}
