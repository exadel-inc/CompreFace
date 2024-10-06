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
import { ChangeDetectionStrategy, Component, OnDestroy, OnInit, Output, EventEmitter } from '@angular/core';
import { Observable } from 'rxjs';
import { filter, first, map, tap } from 'rxjs/operators';
import { Subscription } from 'rxjs';
import { CollectionLeftFacade } from './collection-left-facade';
import { CollectionRightFacade } from '../collection-manager-subject-right/collection-manager-right-facade';
import { CreateDialogComponent } from '../create-dialog/create-dialog.component';
import { TranslateService } from '@ngx-translate/core';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog.component';
import { CircleLoadingProgressEnum } from 'src/app/data/enums/circle-loading-progress.enum';
import { DeleteDialogComponent } from '../delete-dialog/delete-dialog.component';
import { MergerDialogComponent } from '../merger-dialog/merger-dialog.component';
import { EditSubjectDialog } from '../edit-subject/edit-subject-dialog.component';
import { Input } from '@angular/core';

interface OpenDialogOptions {
  subject: string;
  addSubjectInProgress?: boolean;
  isDelete?: boolean;
  isEdit?: boolean;
}

@Component({
  selector: 'app-application-list-container',
  template: `<app-collection-manager-subject-left
    [currentSubject]="currentSubject$ | async"
    [subjectsList]="subjectsList"
    [isCollectionOnHold]="isCollectionOnHold"
    [apiKey]="apiKey$ | async"
    [isPending]="isPending$ | async"
    [search]="search"
    (editSubject)="onEditSubject($event)"
    (deleteSubject)="onDeleteSubject($event)"
    (addSubject)="addSubject($event)"
    (selectedSubject)="onSelectedSubject($event)"
    (initApiKey)="initApiKey($event)"
  ></app-collection-manager-subject-left>`,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CollectionManagerSubjectLeftContainerComponent implements OnInit, OnDestroy {
  currentSubject$: Observable<string>;
  isPending$: Observable<boolean>;
  apiKey$: Observable<string>;

  collectionItemsSubs: Subscription;
  subjectsSubs: Subscription;
  itemsInProgress: boolean;
  subjectsList: string[];

  @Input() isCollectionOnHold: boolean;
  @Input() search: string;
  @Output() setDefaultMode = new EventEmitter();
  private apiKey: string;

  constructor(
    private collectionLeftFacade: CollectionLeftFacade,
    private translate: TranslateService,
    private dialog: MatDialog,
    private collectionRightFacade: CollectionRightFacade
  ) {}

  ngOnInit(): void {
    this.currentSubject$ = this.collectionLeftFacade.currentSubject$;
    this.isPending$ = this.collectionLeftFacade.isPending$;
    this.apiKey$ = this.collectionLeftFacade.apiKey$;

    this.subjectsSubs = this.collectionLeftFacade.subjectsList$.subscribe(subjects => (this.subjectsList = subjects));

    this.collectionItemsSubs = this.collectionRightFacade.collectionItems$
      .pipe(
        map(collection => collection.filter(item => item.status === CircleLoadingProgressEnum.InProgress)),
        tap(collection => (this.itemsInProgress = !!collection.length))
      )
      .subscribe();
  }

  initApiKey(apiKey: string): void {
    this.apiKey = apiKey;
    this.collectionLeftFacade.loadSubjects(apiKey);
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
      .subscribe(() => this.collectionLeftFacade.delete(name, this.apiKey));
  }

  edit(name: string): void {
    const dialog = this.dialog.open(EditSubjectDialog, {
      panelClass: 'custom-mat-dialog',
      data: {
        type: this.translate.instant('manage_collection.right_side.edit_subject'),
        entityName: name,
      },
    });

    dialog
      .afterClosed()
      .pipe(
        first(),
        filter(result => result)
      )
      .subscribe(editName => {
        this.subjectsList.includes(editName) ? this.merger(editName, name) : this.collectionLeftFacade.edit(editName, name, this.apiKey);
      });
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
      .subscribe(() => this.collectionLeftFacade.edit(editName, name, this.apiKey));
  }

  openCreateDialog(): void {
    const dialog = this.dialog.open(CreateDialogComponent, {
      panelClass: 'custom-mat-dialog',
      data: {
        entityType: this.translate.instant('manage_collection.left_side.modal_title'),
        placeholder: this.translate.instant('manage_collection.left_side.subject_name'),
        errorMsg: this.translate.instant('manage_collection.error_msg'),
        nameList: this.subjectsList,
        name: '',
      },
    });

    const dialogSubscription = dialog.afterClosed().subscribe(name => {
      dialogSubscription.unsubscribe();
      if (!name) return;
      this.collectionLeftFacade.addSubject(name, this.apiKey);
    });
  }

  addSubject(subject: string): void {
    this.itemsInProgress
      ? this.openDialog({
          subject,
          addSubjectInProgress: true,
        })
      : this.openCreateDialog();
  }

  onSelectedSubject(subject: string): void {
    this.setDefaultMode.emit();

    if (this.itemsInProgress) {
      this.openDialog({ subject });
    } else {
      this.collectionLeftFacade.onSelectedSubject(subject);
    }
  }

  onDeleteSubject(subject: string): void {
    if (this.itemsInProgress) {
      this.openDialog({
        subject,
        isDelete: true,
      });
    } else {
      this.delete(subject);
    }
  }

  onEditSubject(subject: string): void {
    if (this.itemsInProgress) {
      this.openDialog({
        subject,
        isEdit: true,
      });
    } else {
      this.edit(subject);
    }
  }

  openDialog({ subject, addSubjectInProgress, isDelete, isEdit }: OpenDialogOptions): void {
    const dialog = this.dialog.open(ConfirmDialogComponent, {
      panelClass: 'custom-mat-dialog',
      data: {
        title: this.translate.instant('org_users.confirm_dialog.title'),
        description: this.translate.instant('org_users.confirm_dialog.confirmation_question'),
      },
    });

    dialog.afterClosed().subscribe(confirm => {
      if (!confirm) return;

      this.itemsInProgress = false;

      if (addSubjectInProgress) this.addSubject(subject);

      if (isDelete) {
        this.delete(subject);
      }

      if (isEdit) {
        this.edit(subject);
      }

      this.collectionLeftFacade.onSelectedSubject(subject);
      this.collectionRightFacade.loadSubjectMedia(subject);
    });
  }

  ngOnDestroy() {
    this.collectionItemsSubs.unsubscribe();
    this.subjectsSubs.unsubscribe();
  }
}
