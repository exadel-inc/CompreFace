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
import { Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { Subscription } from 'rxjs';
import { CollectionLeftFacade } from './collection-left-facade';
import { CollectionRightFacade } from '../collection-manager-subject-right/collection-manager-right-facade';
import { CreateDialogComponent } from '../create-dialog/create-dialog.component';
import { TranslateService } from '@ngx-translate/core';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog.component';
import { CircleLoadingProgressEnum } from 'src/app/data/enums/circle-loading-progress.enum';

@Component({
  selector: 'app-application-list-container',
  template: `<app-collection-manager-subject-left
    [subjectsList]="subjectsList$ | async"
    [currentSubject]="currentSubject$ | async"
    [apiKey]="apiKey$ | async"
    [isPending]="isPending$ | async"
    (addSubject)="addSubject()"
    (selectedSubject)="onSelectedSubject($event)"
    (initApiKey)="initApiKey($event)"
  ></app-collection-manager-subject-left>`,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CollectionManagerSubjectLeftContainerComponent implements OnInit, OnDestroy {
  currentSubject$: Observable<string>;
  subjectsList$: Observable<string[]>;
  isPending$: Observable<boolean>;
  apiKey$: Observable<string>;
  collectionItemsSubs: Subscription;
  itemsInProgress: boolean;

  private apiKey: string;

  constructor(
    private collectionLeftFacade: CollectionLeftFacade,
    private translate: TranslateService,
    private dialog: MatDialog,
    private collectionRightFacade: CollectionRightFacade
  ) {}

  ngOnInit(): void {
    this.currentSubject$ = this.collectionLeftFacade.currentSubject$;
    this.subjectsList$ = this.collectionLeftFacade.subjectsList$;
    this.isPending$ = this.collectionLeftFacade.isPending$;
    this.apiKey$ = this.collectionLeftFacade.apiKey$;
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

  addSubject(): void {
    const dialog = this.dialog.open(CreateDialogComponent, {
      panelClass: 'custom-mat-dialog',
      data: {
        entityType: this.translate.instant('manage_collection.left_side.modal_title'),
        placeholder: this.translate.instant('manage_collection.left_side.subject_name'),
        name: '',
      },
    });

    const dialogSubscription = dialog.afterClosed().subscribe(name => {
      if (name) {
        this.collectionLeftFacade.addSubject(name, this.apiKey);
        dialogSubscription.unsubscribe();
      }
    });
  }

  onSelectedSubject(subject): void {
    if (this.itemsInProgress) {
      this.openDialog(subject);
    } else {
      this.collectionLeftFacade.onSelectedSubject(subject);
      this.collectionRightFacade.loadSubjectMedia(subject);
    }
  }

  openDialog(subject): void {
    const dialog = this.dialog.open(ConfirmDialogComponent, {
      panelClass: 'custom-mat-dialog',
    });

    dialog.afterClosed().subscribe(confirm => {
      if (confirm) {
        this.collectionLeftFacade.onSelectedSubject(subject);
        this.collectionRightFacade.loadSubjectMedia(subject);
      }

      this.itemsInProgress = false;
    });
  }

  ngOnDestroy() {
    this.collectionItemsSubs.unsubscribe();
  }
}
