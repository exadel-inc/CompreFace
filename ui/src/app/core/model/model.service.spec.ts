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
import { Model } from 'src/app/data/interfaces/model';

import { environment } from '../../../environments/environment';
import { ModelService } from './model.service';

describe('ModelService', () => {
  let httpMock: HttpTestingController;
  const mockModels: Model[] = [
    {
      id: '0',
      name: 'Model 1',
      type: 'RECOGNITION',
      accessLevel: 'OWNER/TRAIN/READONLY',
      relations: [
        {
          id: 'app_0',
          shareMode: 'NONE',
        },
      ],
      owner: {
        id: '0',
        firstName: 'Owner 0',
        lastName: 'lastname owner 0',
      },
      role: 'USER',
    },
    {
      id: '1',
      name: 'Model 2',
      type: 'RECOGNITION',
      accessLevel: 'OWNER/TRAIN/READONLY',
      relations: [
        {
          id: 'app_0',
          shareMode: 'NONE',
        },
      ],
      owner: {
        id: '0',
        firstName: 'Owner 0',
        lastName: 'lastname owner 0',
      },
      role: 'USER',
    },
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    const service: ModelService = TestBed.inject(ModelService);
    expect(service).toBeTruthy();
  });

  it('should return models array', () => {
    const service: ModelService = TestBed.inject(ModelService);
    service.getAll('0').subscribe(data => {
      expect(data).toEqual(mockModels);
    });

    const req = httpMock.expectOne(`${environment.adminApiUrl}app/0/models`);
    expect(req.request.method).toBe('GET');
    req.flush(mockModels);
  });

  it('should return created model', () => {
    const service: ModelService = TestBed.inject(ModelService);
    service.create('app_0', 'new model', 'RECOGNITION').subscribe(data => {
      expect(data.name).toEqual('new model');
      expect(data.relations[0].id).toEqual('app_0');
    });
    const req = httpMock.expectOne(`${environment.adminApiUrl}app/app_0/model`);
    expect(req.request.method).toBe('POST');
    req.flush({
      id: '2',
      name: 'new model',
      accessLevel: 'OWNER/TRAIN/READONLY',
      relations: [
        {
          id: 'app_0',
          shareMode: 'NONE',
        },
      ],
      owner: {
        id: '0',
        firstName: 'Owner 0',
        lastName: 'lastname owner 0',
      },
    });
  });
});
