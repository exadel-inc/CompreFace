import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ModelHeaderComponent} from './model-header.component';
import {RouterModule} from '@angular/router';
import {MatButtonModule} from '@angular/material/button';
import {EntityTitleModule} from '../entity-title/entity-title.module';
import {ModelHeaderFacade} from './model-header.facade';
import {MatCardModule} from '@angular/material';

@NgModule({
  declarations: [ModelHeaderComponent],
  exports: [
    ModelHeaderComponent
  ],
  imports: [
    CommonModule,
    RouterModule,
    MatButtonModule,
    EntityTitleModule,
    MatCardModule
  ],
  providers: [ModelHeaderFacade],
})
export class ModelHeaderModule { }
