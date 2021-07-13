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
import { CreateDialogComponent } from '../create-dialog/create-dialog.component';
import { TranslateService } from '@ngx-translate/core';
import { MatDialog } from '@angular/material/dialog';
import { CollectionLeftFacade } from './collection-left-facade';
import { MatListOption } from '@angular/material/list';

@Component({
  selector: 'app-collection-manager-subject-left',
  templateUrl: './collection-manager-subject-left.component.html',
  styleUrls: ['./collection-manager-subject-left.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CollectionManagerSubjectLeftComponent implements OnInit, OnDestroy {
  @Input() searchPlaceholder: string;
  @Input() subjectsList: string[];
  @Input() subject: string;
  @Input() isPending: boolean;

  search = '';

  constructor(private translate: TranslateService, private dialog: MatDialog, private collectionLeftFacade: CollectionLeftFacade) {}

  ngOnInit(): void {
    this.collectionLeftFacade.initUrlBindingStreams();
  }

  openModalWindow(): void {
    const dialog = this.dialog.open(CreateDialogComponent, {
      panelClass: 'custom-mat-dialog',
      data: {
        entityType: this.translate.instant('manage_collection.left_side.modal_title'),
        name: '',
      },
    });

    const dialogSubscription = dialog.afterClosed().subscribe(name => {
      if (name) {
        this.collectionLeftFacade.addSubject(name);
        dialogSubscription.unsubscribe();
      }
    });
  }

  onSelectedSubject(change: MatListOption[]): void {
    const { value } = change[0];
    this.collectionLeftFacade.selectedSubject(value);
  }

  onSearch(event: string): void {
    this.search = event;
  }

  ngOnDestroy(): void {
    this.collectionLeftFacade.unsubscribe();
  }
}
