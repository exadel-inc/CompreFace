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

import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {OrganizationComponent} from './organization.component';
import {RouterModule} from '@angular/router';
import {AuthGuard} from '../../core/auth/auth.guard';
import {ToolBarModule} from '../../features/tool-bar/tool-bar.module';
import {OrganizationHeaderModule} from '../../features/organization-header/organization-header.module';
import {OrganizationService} from './organization.service';
import {ApplicationListModule} from 'src/app/features/application-list/application-list.module';
import {MatButtonModule} from '@angular/material/button';
import {TableModule} from 'src/app/features/table/table.module';
import {UserTableModule} from 'src/app/features/user-table/user-table.module';
import {UserListModule} from 'src/app/features/user-list/user-list.module';
import {MatCardModule} from '@angular/material/card';
import {OrganizationCreateComponent} from './organization-create.component';
import {MatFormFieldModule, MatIconModule, MatInputModule} from '@angular/material';
import {CreateOrganisationDialogComponent} from './create-organisation.dialog/create-organisation.dialog';
import {FormsModule} from '@angular/forms';
import {OrganizationUtilsService} from '../../core/organization-utils/organization.service';

@NgModule({
  declarations: [
    OrganizationComponent,
    OrganizationCreateComponent,
    CreateOrganisationDialogComponent
  ],
  imports: [
    ApplicationListModule,
    UserListModule,
    TableModule,
    UserTableModule,
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    FormsModule,
    RouterModule.forChild([
      {path: '', component: OrganizationCreateComponent, canActivate: [AuthGuard]},
      {path: ':id', component: OrganizationComponent, canActivate: [AuthGuard]}
    ]),
    ToolBarModule,
    OrganizationHeaderModule,
    MatCardModule,
  ],
  providers: [OrganizationService, OrganizationUtilsService],
  exports: [RouterModule],
  entryComponents: [
    CreateOrganisationDialogComponent,
  ]
})
export class OrganizationModule { }
