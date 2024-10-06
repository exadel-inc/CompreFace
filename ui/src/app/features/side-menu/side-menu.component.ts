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

import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { CircleLoadingProgressEnum } from 'src/app/data/enums/circle-loading-progress.enum';
import { Routes } from 'src/app/data/enums/routers-url.enum';
import { ServiceTypes } from 'src/app/data/enums/service-types.enum';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog.component';
import { SideMenuFacade } from './side-menu.facade';

@Component({
  selector: 'side-menu',
  templateUrl: './side-menu.component.html',
  styleUrls: ['./side-menu.component.scss'],
})
export class SideMenuComponent implements OnInit, OnDestroy {
  app: string;
  model: string;
  type: string;
  closed: boolean = false;
  recognition: string = ServiceTypes.Recognition;
  currentPage: string;
  itemsInProgress: boolean;
  subs: Subscription;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private dialog: MatDialog,
    private sideMenuFacade: SideMenuFacade,
    private translate: TranslateService
  ) {
    this.subs = this.sideMenuFacade.collectionItems$
      .pipe(
        map(collection => !!collection.find(item => item.status === CircleLoadingProgressEnum.InProgress)),
        tap(res => (this.itemsInProgress = res))
      )
      .subscribe();
  }

  ngOnInit() {
    this.app = this.route.snapshot.queryParams.app;
    this.model = this.route.snapshot.queryParams.model;
    this.type = this.route.snapshot.queryParams.type;
    this.currentPage = '/' + this.route.snapshot.url[0]?.path;
  }

  get routes(): typeof Routes {
    return Routes;
  }

  onDashBoard(path: string): void {
    const queryParams = {
      app: this.app,
      model: this.model,
      type: this.type,
    };

    this.itemsInProgress
      ? this.openDialog(path, queryParams)
      : this.router.navigate([path], {
          queryParams: queryParams,
        });
  }

  onTest(path: string): void {
    const queryParams = {
      app: this.app,
      model: this.model,
      type: this.type,
    };

    this.itemsInProgress
      ? this.openDialog(path, queryParams)
      : this.router.navigate([path], {
          queryParams: queryParams,
        });
  }

  onManageCollection(path: string): void {
    this.router.navigate([path], {
      queryParams: {
        app: this.app,
        model: this.model,
        type: this.type,
      },
    });
  }

  openDialog(path: string, queryParams: { app: string; model: string; type: string }): void {
    const dialog = this.dialog.open(ConfirmDialogComponent, {
      panelClass: 'custom-mat-dialog',
      data: {
        title: this.translate.instant('org_users.confirm_dialog.title'),
        description: this.translate.instant('org_users.confirm_dialog.confirmation_question'),
      },
    });

    dialog.afterClosed().subscribe(confirm => {
      if (!confirm) return;
      this.router.navigate([path], { queryParams: queryParams });
    });
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }
}
