/*!
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
import { TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { filter, first, map, tap } from 'rxjs/operators';
import { CircleLoadingProgressEnum } from 'src/app/data/enums/circle-loading-progress.enum';
import { SubjectModeEnum } from 'src/app/data/enums/subject-mode.enum';
import { CollectionRightFacade } from '../collection-manager-subject-right/collection-manager-right-facade';
import { DeleteDialogComponent } from '../delete-dialog/delete-dialog.component';

@Component({
  selector: 'collection-manager',
  templateUrl: './collection-manager.component.html',
  styleUrls: ['./collection-manager.component.scss'],
})
export class CollectionManagerComponent implements OnInit, OnDestroy {
  subjectModeEnum = SubjectModeEnum;
  mode = SubjectModeEnum.Default;

  subs: Subscription;
  modelSubs: Subscription;

  isCollectionOnHold: boolean;
  selectedIds: string[];
  currentModelName: string;
  search: string = '';

  constructor(private collectionRightFacade: CollectionRightFacade, private translate: TranslateService, private dialog: MatDialog) {}

  ngOnInit(): void {
    this.modelSubs = this.collectionRightFacade.currentModelName$
      .pipe(
        filter(val => !!val),
        tap(val => (this.currentModelName = val))
      )
      .subscribe();

    this.subs = this.collectionRightFacade.collectionItems$
      .pipe(
        tap(items => (this.isCollectionOnHold = !!items.filter(item => item.status === CircleLoadingProgressEnum.OnHold).length)),
        map(items => (this.selectedIds = items.filter(item => item.isSelected).map(item => item.id)))
      )
      .subscribe();
  }

  setBulkSelectMode(): void {
    this.mode = SubjectModeEnum.BulkSelect;
    this.collectionRightFacade.setSubjectMode(this.mode);
  }

  setDefaultMode(): void {
    this.mode = SubjectModeEnum.Default;
    this.collectionRightFacade.setSubjectMode(this.mode);
  }

  deleteSelectedExamples(): void {
    const dialog = this.dialog.open(DeleteDialogComponent, {
      panelClass: 'custom-mat-dialog',
      data: {
        entityType: this.translate.instant('manage_collection.right_side.modal_bulk-delete_type'),
      },
    });

    dialog
      .afterClosed()
      .pipe(
        first(),
        filter(result => result)
      )
      .subscribe(() => this.collectionRightFacade.deleteSelectedExamples(this.selectedIds));
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
    this.modelSubs.unsubscribe();
  }
}
