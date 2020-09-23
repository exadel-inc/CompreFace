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

import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { Model } from 'src/app/data/model';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class DragNDropService {
  headers: HttpHeaders;

  constructor(private http: HttpClient) {}

  addFace(file: any, model: Model): Observable<object> {
    const formData = new FormData();
    formData.append('file', file, file.name);
    formData.append('subject', file.name);
    return this.http.post(`${environment.userApiUrl}faces`, formData, {
      headers: { 'x-api-key': model.apiKey}
    });
  }

  recognize(file: any, model: Model): Observable<object> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(`${environment.userApiUrl}recognize`, formData, {
      headers: { 'x-api-key': model.apiKey}
    });
  }

  getAllFaces(model: Model): Observable<object> {
    return this.http.get(`${environment.userApiUrl}faces`, { headers: { 'x-api-key': model.apiKey }});
  }

  train(model: Model): Observable<object> {
    const formData = new FormData();
    return this.http.post(`${environment.userApiUrl}retrain`, formData, { headers: { 'x-api-key': model.apiKey }});
  }
}
