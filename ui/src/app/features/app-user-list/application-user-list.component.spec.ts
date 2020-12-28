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
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { of, Subscription } from 'rxjs';
import { SnackBarModule } from 'src/app/features/snackbar/snackbar.module';

import { UserTablePipeModule } from '../../ui/search-pipe/user-table-filter.module';
import { SpinnerModule } from '../spinner/spinner.module';
import { UserTableModule } from '../user-table/user-table.module';
import { ApplicationUserListFacade } from './application-user-list-facade';
import { ApplicationUserListComponent } from './application-user-list.component';

describe('ApplicationUserListComponent', () => {
  let component: ApplicationUserListComponent;
  let fixture: ComponentFixture<ApplicationUserListComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [ApplicationUserListComponent, TranslatePipe],
        imports: [SpinnerModule, UserTableModule, NoopAnimationsModule, FormsModule, UserTablePipeModule, MatInputModule, SnackBarModule],
        providers: [
          {
            provide: MatDialog,
            useValue: {},
          },
          {
            provide: ApplicationUserListFacade,
            useValue: {
              initSubscriptions: () => of([{}]),
              appUsers$: of([
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
    fixture = TestBed.createComponent(ApplicationUserListComponent);
    component = fixture.componentInstance;
    component.availableRolesSubscription = new Subscription();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });
});
