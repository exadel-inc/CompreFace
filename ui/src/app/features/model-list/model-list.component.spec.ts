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
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { of } from 'rxjs';

import { ModelTableModule } from '../model-table/model-table.module';
import { SpinnerModule } from '../spinner/spinner.module';
import { ModelListFacade } from './model-list-facade';
import { ModelListComponent } from './model-list.component';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';

describe('ModelListComponent', () => {
  let component: ModelListComponent;
  let fixture: ComponentFixture<ModelListComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [ModelListComponent, TranslatePipe],
        imports: [SpinnerModule, ModelTableModule],
        providers: [
          {
            provide: MatDialog,
            useValue: {},
          },
          {
            provide: ModelListFacade,
            useValue: {
              initSubscriptions: () => of([{}]),
              models$: of([
                {
                  id: 0,
                  name: 'name',
                  owner: {
                    firstname: 'firstname',
                  },
                },
              ]),
              isLoading$: of([{}]),
              unsubscribe: () => {},
            },
          },
          { provide: Router, useValue: {} },
          { provide: TranslateService, useValue: {} },
        ],
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(ModelListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });
});
