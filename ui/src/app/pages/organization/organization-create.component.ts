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

import {ChangeDetectionStrategy, Component, OnDestroy, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material';
import {CreateOrganisationDialogComponent} from './create-organisation.dialog/create-organisation.dialog';
import {OrganizationService} from './organization.service';

@Component({
  selector: 'app-organization-create',
  templateUrl: './organization-create.component.html',
  styleUrls: ['./organization.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OrganizationCreateComponent implements OnInit, OnDestroy {
  constructor(
    public dialog: MatDialog,
    private organizationService: OrganizationService,
  ) { }

  openDialog(): void {
    this.dialog.open(CreateOrganisationDialogComponent, {
      width: '320px',
      height: '200px',
      disableClose: true,
      panelClass: 'dialog-container',
    });
  }

  ngOnInit() {
    this.organizationService.initUrlBindingStreams();
  }

  ngOnDestroy() {
    this.organizationService.unSubscribe();
  }
}
