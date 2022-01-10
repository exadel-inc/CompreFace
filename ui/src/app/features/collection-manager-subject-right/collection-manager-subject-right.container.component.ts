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
import { Observable, Subscription } from 'rxjs';

import { TranslateService } from '@ngx-translate/core';
import { MatDialog } from '@angular/material/dialog';
import { CollectionRightFacade } from './collection-manager-right-facade';
import { filter, first, map, tap } from 'rxjs/operators';
import { EditDialogComponent } from '../edit-dialog/edit-dialog.component';
import { DeleteDialogComponent } from '../delete-dialog/delete-dialog.component';
import { MergerDialogComponent } from '../merger-dialog/merger-dialog.component';
import { CollectionItem } from 'src/app/data/interfaces/collection';
import { CircleLoadingProgressEnum } from 'src/app/data/enums/circle-loading-progress.enum';
import { SubjectModeEnum } from 'src/app/data/enums/subject-mode.enum';

@Component({
  selector: 'app-application-right-container',
  template: `<app-collection-manager-subject-right
    [subject]="subject$ | async"
    [subjects]="subjects$ | async"
    [apiKey]="apiKey$ | async"
    [collectionItems]="collectionItems$ | async"
    [isPending]="isPending$ | async"
    [isCollectionPending]="isCollectionPending$ | async"
    [mode]="mode$ | async"
    [selectedIds]="selectedIds$ | async"
    (editSubject)="edit($event)"
    (deleteSubject)="delete($event)"
    (initApiKey)="initApiKey($event)"
    (readFiles)="readFiles($event)"
    (deleteItem)="deleteItem($event)"
    (cancelUploadItem)="cancelUploadItem($event)"
    (setMode)="setSubjectMode($event)"
    (deleteSelectedExamples)="deleteSelectedExamples($event)"
    (loadMore)="loadMore($event)"
    (selectExample)="selectExample($event)"
    (restartUploading)="restartUploading()"
  ></app-collection-manager-subject-right>`,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CollectionManagerSubjectRightContainerComponent implements OnInit, OnDestroy {
  defaultSubjectSubscription: Subscription;
  subjects$: Observable<string[]>;
  subject$: Observable<string>;
  isPending$: Observable<boolean>;
  isCollectionPending$: Observable<boolean>;
  apiKey$: Observable<string>;
  collectionItems$: Observable<CollectionItem[]>;
  selectedIds$: Observable<string[]>;
  mode$: Observable<SubjectModeEnum>;
  apiKeyInitSubscription: Subscription;

  private subjectList: string[];
  private apiKey: string;

  constructor(private collectionRightFacade: CollectionRightFacade, private translate: TranslateService, private dialog: MatDialog) {}

  ngOnInit(): void {
    this.subjects$ = this.collectionRightFacade.subjects$.pipe(tap(subjects => (this.subjectList = subjects)));
    this.subject$ = this.collectionRightFacade.subject$;
    this.apiKey$ = this.collectionRightFacade.apiKey$;
    this.collectionItems$ = this.collectionRightFacade.collectionItems$;
    this.isPending$ = this.collectionRightFacade.isPending$;
    this.isCollectionPending$ = this.collectionRightFacade.isCollectionPending$;
    this.mode$ = this.collectionRightFacade.subjectMode$;
    this.selectedIds$ = this.collectionItems$.pipe(map(items => items.filter(item => item.isSelected).map(item => item.id)));

    this.defaultSubjectSubscription = this.collectionRightFacade.defaultSubject$.subscribe(subject =>
      this.collectionRightFacade.loadSubjectMedia(subject)
    );
  }

  initApiKey(apiKey: string): void {
    this.apiKey = apiKey;
  }

  loadMore(item: CollectionItem): void {
    this.collectionRightFacade.loadNextPage(item['subject'], item['page'], item['totalPages']);
  }

  delete(name: string): void {
    const dialog = this.dialog.open(DeleteDialogComponent, {
      panelClass: 'custom-mat-dialog',
      data: {
        entityType: this.translate.instant('manage_collection.right_side.delete_subject'),
        entityName: name,
      },
    });

    dialog
      .afterClosed()
      .pipe(
        first(),
        filter(result => result)
      )
      .subscribe(() => this.collectionRightFacade.delete(name, this.apiKey));
  }

  edit(name: string): void {
    const dialog = this.dialog.open(EditDialogComponent, {
      panelClass: 'custom-mat-dialog',
      data: {
        entityType: this.translate.instant('manage_collection.right_side.edit_subject'),
        entityName: name,
      },
    });

    dialog
      .afterClosed()
      .pipe(
        first(),
        filter(result => result)
      )
      .subscribe(editName =>
        this.subjectList.includes(editName) ? this.merger(editName, name) : this.collectionRightFacade.edit(editName, name, this.apiKey)
      );
  }

  merger(editName: string, name: string): void {
    const dialog = this.dialog.open(MergerDialogComponent, {
      panelClass: 'custom-mat-dialog',
      data: {
        entityType: this.translate.instant('manage_collection.right_side.edit_subject'),
      },
    });

    dialog
      .afterClosed()
      .pipe(
        first(),
        filter(result => result)
      )
      .subscribe(() => this.collectionRightFacade.edit(editName, name, this.apiKey));
  }

  restartUploading(): void {
    this.collectionRightFacade.restartUploading();
  }

  readFiles(fileList: File[]): void {
    this.collectionRightFacade.addImageFilesToCollection(fileList);
  }

  deleteItem(item: CollectionItem): void {
    if (item.status === CircleLoadingProgressEnum.Uploaded) {
      this.collectionRightFacade.deleteSubjectExample(item);

      return;
    }

    this.collectionRightFacade.deleteItemFromUploadOrder(item);
  }

  cancelUploadItem(item: CollectionItem): void {
    this.collectionRightFacade.deleteItemFromUploadOrder(item);
  }

  setSubjectMode(mode: SubjectModeEnum): void {
    this.collectionRightFacade.setSubjectMode(mode);
  }

  deleteSelectedExamples(ids: string[]): void {
    const dialog = this.dialog.open(DeleteDialogComponent, {
      panelClass: 'custom-mat-dialog',
      data: {
        entityType: this.translate.instant('manage_collection.right_side.modal_bulk-delete_type'),
        entityName: `(${ids.length} ${this.translate.instant('manage_collection.right_side.modal_bulk-delete_name')})`,
      },
    });

    dialog
      .afterClosed()
      .pipe(
        first(),
        filter(result => result)
      )
      .subscribe(() => this.collectionRightFacade.deleteSelectedExamples(ids));
  }

  selectExample(item: CollectionItem): void {
    this.collectionRightFacade.selectSubjectExample(item);
  }

  ngOnDestroy(): void {
    this.defaultSubjectSubscription.unsubscribe();
    this.collectionRightFacade.resetSubjectExamples();
  }
}
