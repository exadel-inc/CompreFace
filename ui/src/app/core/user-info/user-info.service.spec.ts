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

import { environment } from '../../../environments/environment';
import { User } from '../../data/interfaces/user';
import { UserInfoService } from './user-info.service';

describe('UserInfoService', () => {
  let service: UserInfoService;
  let httpMock: HttpTestingController;

  const mockData: User = {
    email: 'email',
    password: 'password',
    firstName: 'string',
    guid: 'guid_0',
    userId: '1',
    lastName: 'string',
    avatar: 'avatar.png',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [UserInfoService],
    });

    service = TestBed.inject(UserInfoService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should return user info', () => {
    service.get().subscribe(data => {
      expect(data).toEqual(mockData);
    });

    const req = httpMock.expectOne(`${environment.adminApiUrl}user/me`);
    expect(req.request.method).toBe('GET');
    req.flush(mockData);
  });
});
