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

@Component({
  selector: 'app-collection-manager-subject-left',
  templateUrl: './collection-manager-subject-left.component.html',
  styleUrls: ['./collection-manager-subject-left.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CollectionManagerSubjectLeftComponent {
  @Input() subjectsList: string[];
  @Input() currentSubject: string;
  @Input() search: string;
  @Input() isPending: boolean;
  @Input() isCollectionOnHold: boolean;
  @Input() set apiKey(value: string) {
    if (!!value) this.initApiKey.emit(value);
  }

  @Output() deleteSubject = new EventEmitter<string>();
  @Output() editSubject = new EventEmitter<string>();
  @Output() addSubject = new EventEmitter<string>();
  @Output() selectedSubject = new EventEmitter<string>();
  @Output() initApiKey = new EventEmitter<string>();

  onSearch(event: string): void {
    this.search = event;
  }

  onSelectedSubject(subject: string): void {
    if (this.currentSubject === subject) return;
    this.selectedSubject.emit(subject);
  }
}
