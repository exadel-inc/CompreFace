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
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { MatDialogModule } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { of, Subscription } from 'rxjs';
import { SpinnerModule } from 'src/app/features/spinner/spinner.module';
import { UserTableModule } from 'src/app/features/user-table/user-table.module';

import { UserTablePipeModule } from '../../ui/search-pipe/user-table-filter.module';
import { InviteDialogModule } from '../invite-dialog/invite-dialog.module';
import { SnackBarService } from '../snackbar/snackbar.service';
import { UserListFacade } from './user-list-facade';
import { UserListComponent } from './user-list.component';

describe('UserListComponent', () => {
  let component: UserListComponent;
  let fixture: ComponentFixture<UserListComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [
          NoopAnimationsModule,
          CommonModule,
          UserTableModule,
          SpinnerModule,
          FormsModule,
          UserTablePipeModule,
          MatInputModule,
          MatDialogModule,
          InviteDialogModule,
        ],
        declarations: [UserListComponent, TranslatePipe],
        providers: [
          {
            provide: SnackBarService,
            useValue: {},
          },
          {
            provide: UserListFacade,
            useValue: {
              initSubscriptions: () => of([{}]),
              users$: of([
                {
                  id: 0,
                  name: 'name',
                  owner: {
                    firstname: 'firstname',
                  },
                },
              ]),
              isLoading$: of([{}]),
              availableRoles$: of([{}]),
              unsubscribe: () => {},
            },
          },
          { provide: TranslateService, useValue: {} },
        ],
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(UserListComponent);
    component = fixture.componentInstance;
    component.availableRolesSubscription = new Subscription();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });
});
