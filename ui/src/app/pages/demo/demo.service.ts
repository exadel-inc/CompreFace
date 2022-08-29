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
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { DemoModel } from '../../data/interfaces/demo-model';
import { DemoStatus } from '../../data/interfaces/demo-status';

@Injectable({
  providedIn: 'root',
})
export class DemoService {
  constructor(private http: HttpClient) {}

  getModel(): Observable<DemoModel> {
    return this.http.get<DemoModel>(`${environment.adminApiUrl}user/demo/model`);
  }

  getStatus(): Observable<DemoStatus> {
    return this.http.get<DemoStatus>(`${environment.userApiUrl}consistence/status`);
  }
}
