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

import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Observable } from 'rxjs';
import { first } from 'rxjs/operators';

import { Application } from '../../data/interfaces/application';
import { DeleteDialogComponent } from '../delete-dialog/delete-dialog.component';
import { EditDialogComponent } from '../edit-dialog/edit-dialog.component';
import { ApplicationHeaderFacade } from './application-header.facade';
import { Role } from 'src/app/data/enums/role.enum';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-application-header',
  templateUrl: './application-header.component.html',
  styleUrls: ['./application-header.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationHeaderComponent implements OnInit, OnDestroy {
  app$: Observable<Application>;
  userRole$: Observable<string | null>;
  loading$: Observable<boolean>;
  maxHeaderLinkLength = 25;
  userRoleEnum = Role;

  constructor(private applicationHeaderFacade: ApplicationHeaderFacade, private dialog: MatDialog, private translate: TranslateService) {}

  ngOnInit() {
    this.applicationHeaderFacade.initSubscriptions();
    this.app$ = this.applicationHeaderFacade.app$;
    this.userRole$ = this.applicationHeaderFacade.userRole$;
    this.loading$ = this.applicationHeaderFacade.loading$;
  }

  ngOnDestroy(): void {
    this.applicationHeaderFacade.unsubscribe();
  }

  rename(name: string) {
    const dialog = this.dialog.open(EditDialogComponent, {
      width: '300px',
      data: {
        entityType: this.translate.instant('applications.header.title'),
        entityName: name,
      },
    });

    dialog
      .afterClosed()
      .pipe(first())
      .subscribe(result => {
        if (result) {
          this.applicationHeaderFacade.rename(result);
        }
      });
  }

  delete(name: string) {
    const dialog = this.dialog.open(DeleteDialogComponent, {
      width: '400px',
      data: {
        entityType: this.translate.instant('applications.header.title'),
        entityName: name,
      },
    });

    dialog
      .afterClosed()
      .pipe(first())
      .subscribe(result => {
        if (result) {
          this.applicationHeaderFacade.delete();
        }
      });
  }
}
