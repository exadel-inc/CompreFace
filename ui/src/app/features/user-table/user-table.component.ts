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
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import { TranslateService } from '@ngx-translate/core';
import { RoleEditDialogComponent } from '../role-edit-dialog/role-edit-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { UserRole } from '../../data/interfaces/user-role';

@Component({
  selector: 'app-user-table',
  templateUrl: './user-table.component.html',
  styleUrls: ['./user-table.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserTableComponent extends TableComponent implements OnInit, OnChanges {
  message: string;
  noResultMessage: string;
  roleEnum = Role;

  @Input() availableRoles: string[];
  @Input() currentUserId: string;
  @Input() userRole: string;
  @Input() createMessage: string;
  @Input() searchText: string;
  @Output() deleteUser = new EventEmitter<UserDeletion>();

  constructor(private matIconRegistry: MatIconRegistry, private domSanitzer: DomSanitizer, private dialog: MatDialog, private translate: TranslateService) {
    super();
    this.matIconRegistry.addSvgIcon('edit', this.domSanitzer.bypassSecurityTrustResourceUrl('assets/img/icons/edit.svg'));
    this.matIconRegistry.addSvgIcon('trash', this.domSanitzer.bypassSecurityTrustResourceUrl('assets/img/icons/trash.svg'));
  }

  ngOnInit() {
    this.message = this.createMessage;
    this.noResultMessage = this.translate.instant('users.search.no_results');
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

  onEditAppRole(element): void {
    const dialog = this.dialog.open(RoleEditDialogComponent, {
      width: '420px',
      data: {
        element,
        isRoleChangeAllowed: this.isRoleChangeAllowed.bind(this),
        availableRoles: this.availableRoles,
      },
    });

    const dialogSubscription = dialog.afterClosed().subscribe((data: UserRole) => {
      if (data) {
        this.change(data);
        dialogSubscription.unsubscribe();
      }
    });
  }

  delete(user: AppUser): void {
    const deletion: UserDeletion = {
      userToDelete: user,
      deleterUserId: this.currentUserId,
      isDeleteHimSelf: user.id === this.currentUserId,
    };
    this.deleteUser.emit(deletion);
  }

  getMessageContent(): void {
    if (this.searchText.length) {
      this.message = this.noResultMessage;
    } else {
      this.message = this.createMessage;
    }
  }
}
