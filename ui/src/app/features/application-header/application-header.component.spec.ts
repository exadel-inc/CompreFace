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

import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {ApplicationHeaderComponent} from './application-header.component';
import {CommonModule} from '@angular/common';
import {RouterModule} from '@angular/router';
import {MatButtonModule} from '@angular/material/button';
import {EntityTitleModule} from '../entity-title/entity-title.module';
import {ApplicationHeaderFacade} from './application-header.facade';
import {SpinnerModule} from '../spinner/spinner.module';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import {Subject} from 'rxjs';
import {TranslateService} from "@ngx-translate/core";

describe('ApplicationHeaderComponent', () => {
  let component: ApplicationHeaderComponent;
  let fixture: ComponentFixture<ApplicationHeaderComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ApplicationHeaderComponent ],
      providers: [
        {
          provide: ApplicationHeaderFacade,
          useValue: {
            rename: () => {},
            initSubscriptions: () => {},
            unsubscribe: () => {},
            app$: new Subject(),
          }
        },
        {provide: MatDialog, useValue: {}},
        {provide: TranslateService, useValue: {}},
      ],
      imports: [
        CommonModule,
        RouterModule,
        MatButtonModule,
        SpinnerModule,
        EntityTitleModule,
        MatCardModule,
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ApplicationHeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });
});
