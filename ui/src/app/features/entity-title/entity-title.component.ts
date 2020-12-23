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
import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Observable } from 'rxjs';

import { Application } from '../../data/interfaces/application';
import { Model } from '../../data/interfaces/model';

@Component({
  selector: 'app-entity-title',
  templateUrl: './entity-title.component.html',
  styleUrls: ['./entity-title.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EntityTitleComponent implements OnInit {
  newName: string;
  selectedId: string;
  @Input() options: [Application | Model];
  @Input() selectId$: Observable<any>;
  @Input() entityName: string;
  @Output() selectIdChange = new EventEmitter();

  ngOnInit() {
    this.selectId$.subscribe(e => (this.selectedId = e));
  }

  set selected(value) {
    this.selectedId = value;
    this.selectIdChange.emit(value);
  }

  get selected() {
    return this.selectedId;
  }
}
