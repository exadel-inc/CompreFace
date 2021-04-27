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

import { Application } from '../../../data/interfaces/application';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'app-application-header',
  templateUrl: './application-header.component.html',
  styleUrls: ['./application-header.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationHeaderComponent {
  @Input() app: Application;
  @Input() isLoading: boolean;
  @Input() userRole: string;
  @Output() rename = new EventEmitter();
  @Output() delete = new EventEmitter();

  maxHeaderLinkLength = 25;
  userRoleEnum = Role;

  constructor(private dialog: MatDialog) {}

  onRename(name: string): void {
    this.rename.emit(name);
  }

  onDelete(name: string): void {
    this.delete.emit(name);
  }
}
