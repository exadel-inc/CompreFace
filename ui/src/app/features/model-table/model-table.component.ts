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
import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { Role } from 'src/app/data/enums/role.enum';
import { Model } from 'src/app/data/interfaces/model';

import { TableComponent } from '../table/table.component';

@Component({
  selector: 'app-model-table',
  templateUrl: './model-table.component.html',
  styleUrls: ['./model-table.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ModelTableComponent extends TableComponent {
  @Input() userRole: string;
  @Output() copyApiKey = new EventEmitter<string>();
  @Output() edit = new EventEmitter<Model>();
  @Output() delete = new EventEmitter<Model>();
  @Output() test = new EventEmitter<Model>();
  roleEnum = Role;
}
