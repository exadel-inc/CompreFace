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
  @Input() model: Model;
  @Input() app: Application;
  @Input() hideControls: boolean;
  @Input() modelSelected: boolean;
  @Input() itemsInProgress: boolean;

  @Output() usersList = new EventEmitter<Application>();
  @Output() appSettings = new EventEmitter<Application>();

  constructor(private router: Router, private dialog: MatDialog) {}

  onNavigate(path: string) {
    this.itemsInProgress ? this.openDialog(path) : this.router.navigateByUrl(path);
  }

  openDialog(path: string): void {
    const dialog = this.dialog.open(ConfirmDialogComponent, {
      panelClass: 'custom-mat-dialog',
    });

    dialog.afterClosed().subscribe(confirm => {
      if (!confirm) return;
      this.router.navigateByUrl(path);
    });
  }
}
