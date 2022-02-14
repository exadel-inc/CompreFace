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
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { TranslateModule } from '@ngx-translate/core';
import { TablePipeModule } from 'src/app/ui/search-pipe/table-filter.module';
import { TruncateModule } from 'src/app/ui/truncate-pipe/truncate.module';
import { ManageUsersDialog } from './manage-users.component';

@NgModule({
  declarations: [ManageUsersDialog],
  imports: [CommonModule, MatDialogModule, FormsModule, MatIconModule, TranslateModule, MatButtonModule, TablePipeModule, TruncateModule],
  exports: [ManageUsersDialog],
})
export class ManageUsersModule {}
