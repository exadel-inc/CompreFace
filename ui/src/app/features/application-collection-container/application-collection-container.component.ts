/*!
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

import { ChangeDetectionStrategy } from '@angular/core';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Application } from 'src/app/data/interfaces/application';

@Component({
  selector: 'application-collection-container',
  templateUrl: './application-collection-container.component.html',
  styleUrls: ['./application-collection-container.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationCollectionContainerComponent {
  @Output() selectApp = new EventEmitter();
  @Input() applicationCollection: Application[];
  @Input() isLoading: boolean;
  @Input() totalApplications: number;
}
