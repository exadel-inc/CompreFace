import { NgModule } from '@angular/core';
import { ModelRelationTableComponent } from './model-relation-table.component';
import { MatTableModule, MatFormFieldModule, MatSelectModule, MatButtonModule } from '@angular/material';
import { CommonModule } from '@angular/common';

@NgModule({
  declarations: [ModelRelationTableComponent],
  exports: [
    ModelRelationTableComponent
  ],
  imports: [
    CommonModule,
    MatFormFieldModule,
    MatTableModule,
    MatSelectModule,
    MatButtonModule
  ]
})
export class ModelRelationTableModule { }
