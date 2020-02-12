import {NgModule} from '@angular/core';
import {ModelRelationTableComponent} from './model-relation-table.component';
import {MatTableModule, MatFormFieldModule, MatSelectModule, MatButtonModule} from '@angular/material';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';

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
    MatButtonModule,
    MatIconModule
  ]
})
export class ModelRelationTableModule { }
