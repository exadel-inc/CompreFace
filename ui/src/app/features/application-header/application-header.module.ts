import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ApplicationHeaderComponent} from './application-header.component';
import {RouterModule} from '@angular/router';
import {MatButtonModule} from '@angular/material/button';
import {EntityTitleModule} from '../entity-title/entity-title.module';
import {ApplicationHeaderFacade} from './application-header.facade';
import {SpinnerModule} from '../spinner/spinner.module';
import {MatCardModule} from '@angular/material';

@NgModule({
  declarations: [ApplicationHeaderComponent],
  exports: [
    ApplicationHeaderComponent
  ],
  imports: [
    CommonModule,
    RouterModule,
    MatButtonModule,
    SpinnerModule,
    EntityTitleModule,
    MatCardModule
  ],
  providers: [ApplicationHeaderFacade],
})
export class ApplicationHeaderModule {
}
