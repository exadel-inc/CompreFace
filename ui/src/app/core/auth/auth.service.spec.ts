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
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { provideMockStore } from '@ngrx/store/testing';
import { of } from 'rxjs';

import { environment } from '../../../environments/environment';
import { API } from '../../data/enums/api-url.enum';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  const initialState = {
    isPending: false,
    apiKey: null,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        AuthService,
        FormBuilder,
        provideMockStore(),
        {
          provide: Router,
          useValue: { navigateByUrl: () => {} },
        },
        {
          provide: Store,
          useValue: {
            dispatch: () => {},
            select: () => of(initialState),
          },
        },
      ],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('be able to logIn', () => {
    const dummyUser = {
      firstName: 'firstName',
      password: 'password',
    };

    service.logIn(dummyUser.firstName, dummyUser.password, 'password').subscribe();

    const request = httpMock.expectOne(`${environment.adminApiUrl}${API.Login}`);
    expect(request.request.method).toBe('POST');
  });

  it('be able to signUp', () => {
    const dummyUser = {
      firstName: 'firstName',
      password: 'password',
      lastName: 'lastName',
      email: 'q@q.com',
    };

    service.signUp(dummyUser.firstName, dummyUser.password, dummyUser.email, dummyUser.lastName).subscribe();

    const request = httpMock.expectOne(`${environment.adminApiUrl}${API.Register}`);
    expect(request.request.method).toBe('POST');
  });

  it('be able to change password', () => {
    const payload = {
      oldPassword: 'password1',
      newPassword: 'password2',
    };

    service.changePassword(payload.oldPassword, payload.newPassword).subscribe();

    const request = httpMock.expectOne(`${environment.adminApiUrl}${API.ChangePassword}`);
    expect(request.request.method).toBe('PUT');
  });
});
