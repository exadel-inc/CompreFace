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
import {ApplicationUserListComponent} from './application-user-list.component';
import {ApplicationUserListFacade} from './application-user-list-facade';
import {UserTableModule} from '../user-table/user-table.module';
import {SpinnerModule} from 'src/app/features/spinner/spinner.module';
import {InviteUserModule} from '../invite-user/invite-user.module';
import {UserTablePipeModule} from '../../ui/search-pipe/user-table-filter.module';
import {MatInputModule} from '@angular/material/input';
import {FormsModule} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import {InviteDialogComponent} from '../invite-dialog/invite-dialog.component';
import {InviteDialogModule} from '../invite-dialog/invite-dialog.module';
import {TranslateModule} from '@ngx-translate/core';
import {MatIconModule} from '@angular/material/';

@NgModule({
  declarations: [ApplicationUserListComponent],
  exports: [ApplicationUserListComponent],
  providers: [ApplicationUserListFacade],
  imports: [
    CommonModule,
    UserTableModule,
    SpinnerModule,
    InviteUserModule,
    FormsModule,
    UserTablePipeModule,
    MatInputModule,
    MatButtonModule,
    InviteDialogModule,
    TranslateModule,
    MatIconModule
  ],
  entryComponents: [InviteDialogComponent]
})
export class AppUserListModule {}
