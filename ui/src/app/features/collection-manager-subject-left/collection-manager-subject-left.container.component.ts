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
import { CollectionLeftFacade } from './collection-left-facade';
import { CreateDialogComponent } from '../create-dialog/create-dialog.component';
import { TranslateService } from '@ngx-translate/core';
import { MatDialog } from '@angular/material/dialog';
import { MatListOption } from '@angular/material/list';

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
export class CollectionManagerSubjectLeftContainerComponent implements OnInit {
  currentSubject$: Observable<string>;
  subjectsList$: Observable<string[]>;
  isPending$: Observable<boolean>;
  apiKey$: Observable<string>;

  private apiKey: string;

  constructor(private collectionLeftFacade: CollectionLeftFacade, private translate: TranslateService, private dialog: MatDialog) {
  }

  ngOnInit(): void {
    this.currentSubject$ = this.collectionLeftFacade.currentSubject$;
    this.subjectsList$ = this.collectionLeftFacade.subjectsList$;
    this.isPending$ = this.collectionLeftFacade.isPending$;
    this.apiKey$ = this.collectionLeftFacade.apiKey$;
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

  onSelectedSubject(change: MatListOption[]): void {
    this.collectionLeftFacade.onSelectedSubject(change[0].value);
  }
}
