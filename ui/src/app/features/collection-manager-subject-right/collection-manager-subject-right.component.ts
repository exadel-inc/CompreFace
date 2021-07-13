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
import { ChangeDetectionStrategy, Component, Input, OnDestroy, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { MatDialog } from '@angular/material/dialog';
import { filter, first } from 'rxjs/operators';

import { CollectionRightFacade } from './collection-right-facade';
import { EditDialogComponent } from '../edit-dialog/edit-dialog.component';
import { DeleteDialogComponent } from '../delete-dialog/delete-dialog.component';
import { JoiningSubjectsDialogComponent } from '../joining-subjects-dialog/joining-subjects-dialog.component';

@Component({
  selector: 'app-collection-manager-subject-right',
  templateUrl: './collection-manager-subject-right.component.html',
  styleUrls: ['./collection-manager-subject-right.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CollectionManagerSubjectRightComponent implements OnInit, OnDestroy {
  @Input() subjectsList: string[];
  @Input() currentSubject: string;
  @Input() isPending: boolean;

  constructor(private translate: TranslateService, private dialog: MatDialog, private collectionRightFacade: CollectionRightFacade) {}

  ngOnInit(): void {
    this.collectionRightFacade.initUrlBindingStreams();
  }

  delete(): void {
    const dialog = this.dialog.open(DeleteDialogComponent, {
      panelClass: 'custom-mat-dialog',
      data: {
        entityType: this.translate.instant('manage_collection.right_side.delete_subject'),
        entityName: this.currentSubject,
      },
    });

    dialog
      .afterClosed()
      .pipe(
        first(),
        filter(result => result)
      )
      .subscribe(() => this.collectionRightFacade.deleteSubject(this.currentSubject));
  }

  edit(): void {
    const dialog = this.dialog.open(EditDialogComponent, {
      panelClass: 'custom-mat-dialog',
      data: {
        entityType: this.translate.instant('manage_collection.right_side.edit_subject'),
        entityName: this.currentSubject,
      },
    });

    dialog
      .afterClosed()
      .pipe(
        first(),
        filter(name => name)
      )
      .subscribe(name =>
        this.subjectsList.includes(name)
          ? this.joinSubjects(name, this.currentSubject)
          : this.collectionRightFacade.editSubject(name, this.currentSubject)
      );
  }

  joinSubjects(name: string, subject: string): void {
    const dialog = this.dialog.open(JoiningSubjectsDialogComponent, {
      panelClass: 'custom-mat-dialog',
    });

    dialog
      .afterClosed()
      .pipe(
        first(),
        filter(result => result)
      )
      .subscribe(() => this.collectionRightFacade.editSubject(name, subject));
  }

  ngOnDestroy(): void {
    this.collectionRightFacade.unsubscribe();
  }
}
