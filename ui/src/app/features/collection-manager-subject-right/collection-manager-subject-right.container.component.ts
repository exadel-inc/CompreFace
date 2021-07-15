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
import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';

import { TranslateService } from '@ngx-translate/core';
import { MatDialog } from '@angular/material/dialog';
import { CollectionRightFacade } from './collection-manager-right-facade';
import { filter, first, tap } from 'rxjs/operators';
import { EditDialogComponent } from '../edit-dialog/edit-dialog.component';
import { DeleteDialogComponent } from '../delete-dialog/delete-dialog.component';
import { MergerDialogComponent } from '../merger-dialog/merger-dialog.component';

@Component({
  selector: 'app-application-right-container',
  template: `<app-collection-manager-subject-right
    [subject]="subject$ | async"
    [subjects]="subjects$ | async"
    [isPending]="isPending$ | async"
    (editSubject)="edit($event)"
    (deleteSubject)="delete($event)"
  ></app-collection-manager-subject-right>`,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CollectionManagerSubjectRightContainerComponent implements OnInit {
  subjects$: Observable<string[]>;
  subject$: Observable<string>;
  isPending$: Observable<boolean>;

  private subjectList: string[];

  constructor(private collectionRightFacade: CollectionRightFacade, private translate: TranslateService, private dialog: MatDialog) {
    this.collectionRightFacade.initUrlBindingStreams();
  }

  ngOnInit(): void {
    this.subjects$ = this.collectionRightFacade.subjects$.pipe(tap(subjects => (this.subjectList = subjects)));
    this.subject$ = this.collectionRightFacade.subject$;
    this.isPending$ = this.collectionRightFacade.isPending$;
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
      .subscribe(() => this.collectionRightFacade.delete(name));
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
        this.subjectList.includes(editName) ? this.merger(editName, name) : this.collectionRightFacade.edit(editName, name)
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
      .subscribe(() => this.collectionRightFacade.edit(editName, name));
  }
}
