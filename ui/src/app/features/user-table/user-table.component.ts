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
import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnChanges, OnInit, Output } from '@angular/core';
import { Role } from 'src/app/data/enums/role.enum';
import { AppUser } from 'src/app/data/interfaces/app-user';

import { UserDeletion } from '../../data/interfaces/user-deletion';
import { TableComponent } from '../table/table.component';

@Component({
  selector: 'app-user-table',
  templateUrl: './user-table.component.html',
  styleUrls: ['./user-table.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserTableComponent extends TableComponent implements OnInit, OnChanges {
  messageHeader: string;
  message: string;
  noResultMessage = 'No matches found';
  roleEnum = Role;

  @Input() availableRoles: string[];
  @Input() currentUserId: string;
  @Input() userRole: string;
  @Input() createHeader: string;
  @Input() createMessage: string;
  @Input() searchText: string;
  @Output() deleteUser = new EventEmitter<UserDeletion>();

  ngOnInit() {
    this.messageHeader = this.createHeader;
    this.message = this.createMessage;
  }

  ngOnChanges(): void {
    this.getMessageContent();
  }

  isRoleChangeAllowed(user: AppUser): boolean {
    return (
      user.userId !== this.currentUserId &&
      this.userRole !== Role.User &&
      user.role !== Role.Owner &&
      this.availableRoles.indexOf(user.role) > -1
    );
  }

  delete(user: AppUser): void {
    const deletion: UserDeletion = {
      userToDelete: user,
      deleterUserId: this.currentUserId,
    };
    this.deleteUser.emit(deletion);
  }

  changeRole(event: any, element: AppUser): void {
    this.change({ id: element.id, role: event.value });
  }

  getMessageContent(): void {
    if (this.searchText.length) {
      this.messageHeader = '';
      this.message = this.noResultMessage;
    } else {
      this.messageHeader = this.createHeader;
      this.message = this.createMessage;
    }
  }
}
