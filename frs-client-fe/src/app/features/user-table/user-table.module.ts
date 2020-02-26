import {NgModule} from '@angular/core';
import {UserTableComponent} from './user-table.component';
import {MatTableModule, MatFormFieldModule, MatSelectModule,  MatIconModule} from '@angular/material';
import {CommonModule} from '@angular/common';

@NgModule({
  declarations: [UserTableComponent],
  exports: [
    UserTableComponent
  ],
  imports: [
    CommonModule,
    MatFormFieldModule,
    MatTableModule,
    MatSelectModule,
    MatIconModule
  ]
})
export class UserTableModule { }
