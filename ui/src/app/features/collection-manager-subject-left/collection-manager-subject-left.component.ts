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

import { Component, Input, OnInit } from '@angular/core';
import { CreateDialogComponent } from '../create-dialog/create-dialog.component';
import { TranslateService } from '@ngx-translate/core';
import { MatDialog } from '@angular/material/dialog';
import { CollectionLeftFacade } from './collection-left-facade';
import {selectAddSubjectPending, selectCollectionSubjects} from '../../store/manage-collectiom/selectors';
import { Observable } from 'rxjs';
import { Store } from '@ngrx/store';

@Component({
  selector: 'app-collection-manager-subject-left',
  templateUrl: './collection-manager-subject-left.component.html',
  styleUrls: ['./collection-manager-subject-left.component.scss'],
})
export class CollectionManagerSubjectLeftComponent implements OnInit {
  @Input() searchPlaceholder: string;
  @Input() subjectsList: string[];
  @Input() isPending: boolean;

  search = '';

  constructor(
    private translate: TranslateService,
    private dialog: MatDialog,
    private collectionLeftFacade: CollectionLeftFacade,
    private store: Store<any>
  ) {}

  ngOnInit(): void {
    this.collectionLeftFacade.initUrlBindingStreams();
  }

  openModalWindow() {
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

  onSearch(event: string) {
    this.search = event;
  }
}
