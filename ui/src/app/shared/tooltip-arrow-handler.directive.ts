import { OverlayContainer } from '@angular/cdk/overlay';
import { Directive, ElementRef, HostListener, Renderer2 } from '@angular/core';
import { MatTooltip } from '@angular/material/tooltip';

@Directive({
  selector: '[tooltipArrowHandler]',
})
export class TooltipArrowHandlerDirective {
  constructor(
    private el: ElementRef,
    private renderer: Renderer2,
    private matTooltip: MatTooltip,
    private overlayContainer: OverlayContainer
  ) {}

  @HostListener('mouseenter') onMouseEnter() {
    const position = this.el.nativeElement.getBoundingClientRect();

    if (this.matTooltip) {
      setTimeout(() => {
        const tooltipElements = this.overlayContainer.getContainerElement().querySelectorAll('.mat-tooltip-panel');
        const tooltipElement = Array.from(tooltipElements).find(el => {
          return el.textContent === this.matTooltip.message;
        });

        if (tooltipElement) {
          const tooltipPosition = tooltipElement.getBoundingClientRect();
          if (Math.floor(tooltipPosition.bottom) <= Math.floor(position.top)) {
            this.matTooltip.tooltipClass = 'bottom-arrow';
          } else {
            this.matTooltip.tooltipClass = 'top-arrow';
          }
        }
      }, 0);
    }
  }
}
