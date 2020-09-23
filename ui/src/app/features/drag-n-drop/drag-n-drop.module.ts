import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DragNDropComponent } from './drag-n-drop.component';
import { DragNDropDirective } from './drag-n-drop.directive';
import { TranslateModule } from '@ngx-translate/core';

@NgModule({
  declarations: [DragNDropComponent, DragNDropDirective],
  exports: [DragNDropComponent],
  imports: [
    CommonModule,
    TranslateModule
  ]
})
export class DragNDropModule { }
