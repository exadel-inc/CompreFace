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
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class CollectionService {
  constructor(private http: HttpClient) {}

  getSubjectsList(apiKey: string): Observable<{ subjects: string[] }> {
    return this.http.get<{ subjects: string[] }>(`${environment.userApiUrl}recognition/subjects`, { headers: { 'x-api-key': apiKey } });
  }

  addSubject(name: string, apiKey: string): Observable<{ subject: string }> {
    console.log(name, apiKey);
    return this.http.post<{ subject: string }>(
      `${environment.userApiUrl}recognition/subjects`,
      { subject: name },
      { headers: { 'x-api-key': apiKey } }
    );
  }
}
