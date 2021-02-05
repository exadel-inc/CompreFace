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
import { Model } from 'src/app/data/interfaces/model';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root',
})
export class ModelService {
  constructor(private http: HttpClient) {}

  public getAll(applicationId: string): Observable<Model[]> {
    return this.http.get<Model[]>(`${environment.adminApiUrl}app/${applicationId}/models`);
  }

  public create(applicationId: string, name: string): Observable<Model> {
    name = name.trim();
    return this.http.post<Model>(`${environment.adminApiUrl}app/${applicationId}/model`, { name });
  }

  public update(applicationId: string, modelId: string, name: string): Observable<Model> {
    name = name.trim();
    return this.http.put<Model>(`${environment.adminApiUrl}app/${applicationId}/model/${modelId}`, { name });
  }

  public delete(applicationId: string, modelId: string): Observable<Model> {
    return this.http.delete<Model>(`${environment.adminApiUrl}app/${applicationId}/model/${modelId}`);
  }
}
