/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
import {
  AfterContentInit,
  ContentChild,
  Directive,
  ElementRef,
  HostListener,
  Input,
  OnChanges,
  Renderer2,
  SimpleChanges,
} from '@angular/core';

import { FrameSize } from '../../data/interfaces/frame-size';
import { BoxSize } from '../../data/interfaces/response-result';

@Directive({
  selector: '[appFrameTooltip]',
})
export class FaceServicesDirective implements OnChanges, AfterContentInit {
  @Input('appFrameTooltip') dataFrames: any;

  @ContentChild('boxFace') boxFace: ElementRef;
  @ContentChild('boxInfo') boxInfo: ElementRef;

  private borderColor = 'rgba(255, 255, 255, 0.5)';
  private borderColorActive = '#40BFEF';
  private size: FrameSize;

  constructor(private element: ElementRef, private renderer: Renderer2) {}

  ngOnChanges(changes: SimpleChanges): void {
    // eslint-disable-next-line @typescript-eslint/naming-convention
    const { x_max, x_min, y_max, y_min } = changes.dataFrames.currentValue as BoxSize;
    this.size = {
      top: y_min,
      left: x_min,
      width: x_max - x_min,
      height: y_max - y_min,
    };
  }

  ngAfterContentInit(): void {
    if (this.boxInfo) this.renderer.setStyle(this.boxInfo.nativeElement, 'display', 'none');

    this.addFrame(this.size);
    this.renderer.setStyle(this.boxFace.nativeElement, 'borderColor', this.borderColor);
    this.renderer.setStyle(this.element.nativeElement, 'zIndex', 1);
  }

  @HostListener('mouseenter') onMouseEnter() {
    if (!!this.boxInfo) {
      this.renderer.setStyle(this.boxInfo.nativeElement, 'display', 'block');
      this.renderer.setStyle(this.boxFace.nativeElement, 'borderColor', this.borderColorActive);
    }

    this.renderer.setStyle(this.element.nativeElement, 'zIndex', 2);
  }

  @HostListener('mouseleave') onMouseLeave() {
    this.renderer.setStyle(this.boxFace.nativeElement, 'borderColor', this.borderColor);
    this.renderer.setStyle(this.element.nativeElement, 'zIndex', 1);

    if (!!this.boxInfo) {
      this.renderer.setStyle(this.boxInfo.nativeElement, 'display', 'none');
    }
  }

  addFrame(size: FrameSize): void {
    Object.keys(size).forEach(key => this.renderer.setStyle(this.element.nativeElement, key, `${size[key]}px`));
  }
}
