/*
 * Copyright 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { Role } from 'src/app/data/enums/role.enum';
import { TranslateService } from '@ngx-translate/core';
import { Application } from 'src/app/data/interfaces/application';

@Component({
  selector: 'app-application-list',
  templateUrl: './application-list.component.html',
  styleUrls: ['./application-list.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationListComponent {
  @Input() isLoading: boolean;
  @Input() applicationCollection: Application[];
  @Input() userRole: string;

  @Output() selectApp = new EventEmitter();
  @Output() createApp = new EventEmitter();
  @Output() manageUsers = new EventEmitter();

  roleEnum = Role;
  search = '';

  constructor(private translate: TranslateService) {}

  onSearch(event: string): void {
    this.search = event;
  }

  searchTitle(): string {
    const titleApp: string = this.translate.instant('applications.title');
    const titleCreate: string = this.translate.instant('applications.first_steps_title');

    return this.applicationCollection.length > 0 ? titleApp : titleCreate;
  }
}
