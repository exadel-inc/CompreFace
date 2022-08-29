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

import { ChangeDetectionStrategy, Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-search-table',
  templateUrl: './app-search-table.component.html',
  styleUrls: ['./app-search-table.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppSearchTableComponent {
  @Input() title: string;
  @Input() searchPlaceholder: string;
  @Input() buttonText: string;
  @Input() requiredRole: string;
  @Input() currentRole: string;
  @Input() hideContent: boolean;

  @Output() manageUsersView = new EventEmitter();
  @Output() inputSearch: EventEmitter<string> = new EventEmitter();
  @Output() modalWindow: EventEmitter<MouseEvent> = new EventEmitter();

  onInputChange(event: Event): void {
    const data = event.target as HTMLInputElement;
    this.inputSearch.emit(data.value);
  }

  onButtonChange(event: MouseEvent): void {
    this.modalWindow.emit(event);
  }

  onOpenUserList() {
    this.manageUsersView.emit();
  }
}
