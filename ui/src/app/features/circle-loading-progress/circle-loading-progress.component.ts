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
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  HostBinding,
  Input,
  OnInit,
  Output,
  OnChanges,
  SimpleChanges,
} from '@angular/core';
import { CircleLoadingProgressEnum } from 'src/app/data/enums/circle-loading-progress.enum';

@Component({
  selector: 'circle-progress',
  templateUrl: './circle-loading-progress.component.html',
  styleUrls: ['./circle-loading-progress.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CircleLoadingProgressComponent {
  @Input() state = CircleLoadingProgressEnum.InProgress;
  @Input() error: string;

  @Output() cancel = new EventEmitter();

  inProgressDiameter = 80;
  inProgressStrokeWidth = 4;

  progressEnum = CircleLoadingProgressEnum;

  @HostBinding('class.blurred') isBlurred: boolean = false;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.state?.currentValue) {
      this.isBlurred = this.state !== CircleLoadingProgressEnum.Uploaded;
    }
  }
}
