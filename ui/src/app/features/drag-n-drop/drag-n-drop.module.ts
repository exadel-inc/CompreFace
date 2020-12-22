import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { DragNDropDirective } from './drag-n-drop.directive';
import { DragNDropComponent } from './drag-n-drop.component';

@NgModule({
  declarations: [DragNDropComponent, DragNDropDirective],
  exports: [DragNDropComponent],
  imports: [CommonModule, TranslateModule],
})
export class DragNDropModule {}
