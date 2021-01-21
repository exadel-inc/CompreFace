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
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatToolbarModule } from '@angular/material/toolbar';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';

import { ToolBarComponent } from './tool-bar.component';
import { ToolBarContainerComponent } from './tool-bar.container.component';
import { ToolBarFacade } from './tool-bar.facade';
import { ChangePasswordDialogComponent } from '../change-password-dialog/change-password-dialog.component';
import { ChangePasswordDialogModule } from '../change-password-dialog/change-password-dialog.module';
import { EditUserInfoDialogComponent } from '../edit-user-info-dialog/edit-user-info-dialog.component';
import { EditUserInfoDialogModule } from '../edit-user-info-dialog/edit-user-info-dialog.module';

@NgModule({
  declarations: [ToolBarContainerComponent, ToolBarComponent],
  exports: [ToolBarContainerComponent, ToolBarComponent],
  imports: [
    CommonModule,
    MatToolbarModule,
    MatButtonModule,
    MatMenuModule,
    MatIconModule,
    RouterModule,
    TranslateModule,
    ChangePasswordDialogModule,
    EditUserInfoDialogModule,
  ],
  providers: [ToolBarFacade],
  entryComponents: [ChangePasswordDialogComponent, EditUserInfoDialogComponent],
})
export class ToolBarModule {}
