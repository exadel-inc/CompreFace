/*!
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

import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule } from '@angular/router';
import { AppUserListModule } from 'src/app/features/app-user-list/application-user-list.module';
import { ApplicationListModule } from 'src/app/features/application-list/application-list.module';
import { BreadcrumbsContainerModule } from 'src/app/features/breadcrumbs.container/breadcrumbs.container.module';
import { BreadcrumbsModule } from 'src/app/features/breadcrumbs/breadcrumbs.module';
import { ModelListModule } from 'src/app/features/model-list/model-list.module';
import { TableModule } from 'src/app/features/table/table.module';
import { ToolBarModule } from 'src/app/features/tool-bar/tool-bar.module';
import { UserListModule } from 'src/app/features/user-list/user-list.module';
import { UserTableModule } from 'src/app/features/user-table/user-table.module';
import { ApplicationUsersComponent } from './application-users.component';

@NgModule({
  declarations: [ApplicationUsersComponent],
  imports: [
    CommonModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    ModelListModule,
    AppUserListModule,
    ApplicationListModule,
    ToolBarModule,
    BreadcrumbsModule,
    BreadcrumbsContainerModule,
    UserListModule,
    TableModule,
    UserTableModule,
    CommonModule,
    RouterModule.forChild([{ path: '', component: ApplicationUsersComponent }]),
  ],
})
export class ApplicationUsersModule {}
