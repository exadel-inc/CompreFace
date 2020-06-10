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
import {ApplicationListContainerComponent} from './application-list-container.component';
import {MatDialog} from '@angular/material';
import {SpinnerModule} from '../spinner/spinner.module';
import {TableModule} from '../table/table.module';
import {ApplicationListFacade} from './application-list-facade';
import {Router} from '@angular/router';
import {of} from 'rxjs';

describe('ApplicationListContainerComponent', () => {
  let component: ApplicationListContainerComponent;
  let fixture: ComponentFixture<ApplicationListContainerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ApplicationListContainerComponent],
      imports: [SpinnerModule, TableModule],
      providers: [{
        provide: MatDialog,
        useValue: {}
      }, {
        provide: ApplicationListFacade,
        useValue: {
          initSubscriptions: () => of([{}]),
          applications$: of([{
            id: 0,
            name: 'name',
            owner: {
              firstname: 'firstname'
            }
          }]),
          selectedOrganization$: of([{}]),
          isLoading$: of([{}]),
          unsubscribe() {
          }
        }
      }, {
        provide: Router,
        useValue: {}
      }]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ApplicationListContainerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
