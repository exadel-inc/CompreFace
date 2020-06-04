import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ToolBarComponent} from './tool-bar.component';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonModule} from '@angular/material/button';
import {MatMenuModule} from '@angular/material/menu';
import {MatIconModule} from '@angular/material/icon';
import {RouterModule} from '@angular/router';

@NgModule({
  declarations: [ToolBarComponent],
  exports: [
    ToolBarComponent,
  ],
  imports: [
    CommonModule,
    MatToolbarModule,
    MatButtonModule,
    MatMenuModule,
    MatIconModule,
    RouterModule
  ]
})
export class ToolBarModule { }
