import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatIconModule } from '@angular/material';
import { MatButtonModule } from '@angular/material/button';
import { SpinnerModule } from 'src/app/features/spinner/spinner.module';

import { ModelTableModule } from '../model-table/model-table.module';
import { ModelListFacade } from './model-list-facade';
import { ModelListComponent } from './model-list.component';

@NgModule({
  declarations: [ModelListComponent],
  exports: [ModelListComponent],
  providers: [ModelListFacade],
  imports: [
    CommonModule,
    SpinnerModule,
    MatButtonModule,
    MatIconModule,
    ModelTableModule,
  ]
})
export class ModelListModule { }
