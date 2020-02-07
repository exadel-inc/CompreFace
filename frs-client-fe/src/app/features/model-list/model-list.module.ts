import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {ModelListComponent} from './model-list.component';
import {ModelListFacade} from './model-list-facade';
import {TableModule} from '../table/table.module';
import {SpinnerModule} from 'src/app/features/spinner/spinner.module';
import {MatButtonModule} from '@angular/material/button';

@NgModule({
  declarations: [ModelListComponent],
  exports: [ModelListComponent],
  providers: [ModelListFacade],
  imports: [CommonModule, TableModule, SpinnerModule, MatButtonModule]
})
export class ModelListModule { }
