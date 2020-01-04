import { NgModule } from '@angular/core';
import { TableComponent } from './table.component';
import { MatTableModule } from '@angular/material';
import { CommonModule } from '@angular/common';

@NgModule({
  declarations: [TableComponent],
  exports: [
    TableComponent
  ],
  imports: [
    CommonModule,
    MatTableModule
  ]
})
export class TableModule { }
