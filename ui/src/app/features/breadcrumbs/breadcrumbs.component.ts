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
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { Role } from 'src/app/data/enums/role.enum';

import { Routes } from '../../data/enums/routers-url.enum';
import { Application } from '../../data/interfaces/application';
import { Model } from '../../data/interfaces/model';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-breadcrumbs',
  templateUrl: './breadcrumbs.component.html',
  styleUrls: ['./breadcrumbs.component.scss'],
})
export class BreadcrumbsComponent {
  routes = Routes;
  maxNameLength = 30;
  role = Role;
  @Input() model: Model;
  @Input() app: Application;
  @Input() hideControls: boolean;
  @Input() modelSelected: boolean;
  @Input() itemsInProgress: boolean;
  @Input() currentUserRole: string;

  @Output() usersList = new EventEmitter<Application>();
  @Output() appSettings = new EventEmitter<Application>();

  constructor(private router: Router, private dialog: MatDialog, private translate: TranslateService) {}

  onNavigate(path: string, id?: string) {
    this.itemsInProgress ? this.openDialog(path, id) : this.router.navigate([path], { queryParams: { app: id } });
  }

  openDialog(path: string, id?: string): void {
    const dialog = this.dialog.open(ConfirmDialogComponent, {
      panelClass: 'custom-mat-dialog',
      data: {
        title: this.translate.instant('org_users.confirm_dialog.title'),
        description: this.translate.instant('org_users.confirm_dialog.confirmation_question'),
      },
    });

    dialog.afterClosed().subscribe(confirm => {
      if (!confirm) return;
      this.router.navigate([path], { queryParams: { app: id } });
    });
  }
}
