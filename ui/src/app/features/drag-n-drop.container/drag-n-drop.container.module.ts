import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DragNDropContainerComponent } from './drag-n-drop.container.component';
import { DragNDropModule } from '../drag-n-drop/drag-n-drop.module';

@NgModule({
  declarations: [DragNDropContainerComponent],
  exports: [
    DragNDropContainerComponent
  ],
  imports: [
    CommonModule,
    DragNDropModule
  ]
})
export class DragNDropContainerModule { }
