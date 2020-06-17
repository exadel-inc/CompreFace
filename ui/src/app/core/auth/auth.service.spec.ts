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

import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth.service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { API_URL } from '../../data/api.variables';
import { FormBuilder } from '@angular/forms';
import { Router } from '@angular/router';
import { provideMockStore } from '@ngrx/store/testing';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    localStorage.setItem('token', 'some token');
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        AuthService,
        FormBuilder,
        provideMockStore(),
        {
          provide: Router,
          useValue: { navigateByUrl: () => { } }
        }]
    });
    service = TestBed.get(AuthService);
    httpMock = TestBed.get(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('be able to logIn', () => {
    const dummyUser = {
      firstName: 'firstName',
      password: 'password'
    };

    const dummyToken = 'Some token';

    service.logIn(dummyUser.firstName, dummyUser.password).subscribe(token => {
      expect(token).toEqual(dummyToken);
    });

    const request = httpMock.expectOne(`${environment.apiUrl}${API_URL.LOGIN}`);
    expect(request.request.method).toBe('POST');
    request.flush(dummyToken);
  });

  it('be able to signUp', () => {
    const dummyUser = {
      firstName: 'firstName',
      password: 'password',
      lastName: 'lastName',
      email: 'q@q.com'
    };

    const dummyToken = 'Some token';

    service.signUp(dummyUser.firstName, dummyUser.password, dummyUser.email, dummyUser.lastName).subscribe(response => {
      expect(response.status).toEqual(201);
    });

    const request = httpMock.expectOne(`${environment.apiUrl}${API_URL.REGISTER}`);
    expect(request.request.method).toBe('POST');
    request.flush(dummyToken, { status: 201, statusText: 'Created' });
  });

  it('be able to get token', () => {
    expect(service.getToken()).toEqual('some token');
  });

  it('be able to update token', () => {
    expect(service.getToken()).toEqual('some token');
    service.updateTokens('token the second value', 'refreshToken value');
    expect(service.getToken()).toEqual('Bearer token the second value');
  });

  it('be able to remove token', () => {
    expect(service.getToken()).toEqual('some token');
    service.removeToken();
    expect(service.getToken()).toEqual(null);
  });
});
