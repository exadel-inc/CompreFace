import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TooltipArrowHandlerDirective } from './tooltip-arrow-handler.directive';

@NgModule({
  declarations: [TooltipArrowHandlerDirective],
  imports: [CommonModule],
  exports: [TooltipArrowHandlerDirective],
})
export class SharedModule {}
