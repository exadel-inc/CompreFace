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

import {Component} from '@angular/core';
import {MatDialogRef} from '@angular/material';

import {OrganizationService} from '../organization.service';
import {Router} from '@angular/router';
import {ROUTERS_URL} from '../../../data/routers-url.variable';

@Component({
  selector: 'app-create-organisation-dialog',
  templateUrl: './create-organisation.dialog.html',
  styleUrls: ['./create-organisation.dialog.scss'],
})
export class CreateOrganisationDialogComponent {
  public name = '';

  constructor(
    public dialogRef: MatDialogRef<CreateOrganisationDialogComponent>,
    private organizationService: OrganizationService,
    private router: Router,
  ) { }

  public close() {
    this.dialogRef.close();
  }

  public submit() {
    this.organizationService.createOrganization(this.name)
      .subscribe((org) => {
        this.close();
        this.router.navigate([ROUTERS_URL.ORGANIZATION, org.id]);
      });
  }
}
