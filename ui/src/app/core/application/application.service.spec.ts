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

import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {ApplicationService} from './application.service';
import {Application} from 'src/app/data/application';
import {environment} from '../../../environments/environment';

describe('ApplicationService', () => {
  let service: ApplicationService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ApplicationService]
    });

    service = TestBed.get(ApplicationService);
    httpMock = TestBed.get(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should return set of applications', () => {
    service.getAll('organizationId').subscribe((data: Application[]) => {
      expect(data.length).toBe(2);

      expect(data[0].name).toBe('test-application-one');
      expect(data[0].organizationId).toBe('organizationId');

      expect(data[1].name).toBe('test-application-two');
      expect(data[1].organizationId).toBe('organizationId');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}org/organizationId/apps`);
    expect(req.request.method).toBe('GET');
    req.flush([{
      name: 'test-application-one',
      id: '0',
      owner: 'owner',
      organizationId: 'organizationId'
    }, {
      name: 'test-application-two',
      id: '1',
      owner: 'owner',
      organizationId: 'organizationId'
    }]);
  });

  it('should return set of applications', () => {
    service.create('organizationId', 'new-app').subscribe((data: Application) => {
      expect(data.name).toBe('new-app');
      expect(data.organizationId).toBe('organizationId');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}org/organizationId/app`);
    expect(req.request.method).toBe('POST');
    req.flush({
      name: 'new-app',
      id: '2',
      owner: 'owner',
      organizationId: 'organizationId'
    });
  });

  it('should rename application', () => {
    service.put('orgId', 'appId', 'new name').subscribe((data: Application) => {
      expect(data.name).toBe('new app');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}org/orgId/app/appId`);
    expect(req.request.method).toBe('PUT');
    req.flush({
      name: 'new app',
      id: '2',
      owner: 'owner',
      organizationId: 'orgId'
    });
  });
});
