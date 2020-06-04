import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatIconModule, MatMenuModule, MatTableModule } from '@angular/material';
import { MatButtonModule } from '@angular/material/button';

import { ModelTableComponent } from './model-table.component';

@NgModule({
  declarations: [ModelTableComponent],
  exports: [ModelTableComponent],
  imports: [
    CommonModule,
    MatButtonModule,
    MatTableModule,
    MatIconModule,
    MatMenuModule,
  ]
})
export class ModelTableModule { }
