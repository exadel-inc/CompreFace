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
import { Observable, of } from 'rxjs';
import { Model } from 'src/app/data/interfaces/model';
import { environment } from 'src/environments/environment';
import { map, switchMap } from 'rxjs/operators';
import { DemoService } from '../../pages/demo/demo.service';

@Injectable({
  providedIn: 'root',
})
export class ModelService {
  constructor(private http: HttpClient, private demoService: DemoService) {}

  // Don't show 'Demo recognition service' when we're using build the compreface-core:0.5.0-mobilenet
  filterModel(models: Model[]): Observable<Model[]> {
    const apiKeyDemoService = '00000000-0000-0000-0000-000000000002';

    return of(false).pipe(
      switchMap(() =>
        this.demoService
          .getStatus()
          .pipe(
            map(({ demoFaceCollectionIsInconsistent }) =>
              demoFaceCollectionIsInconsistent ? models.filter(model => model.apiKey !== apiKeyDemoService) : models
            )
          )
      )
    );
  }

  getAll(applicationId: string): Observable<Model[]> {
    return this.http
      .get<Model[]>(`${environment.adminApiUrl}app/${applicationId}/models`)
      .pipe(switchMap(models => this.filterModel(models)));
  }

  // replace any later =============
  getStatistics(appId: string, modelId: string): Observable<any> {
    // return this.http.get(`${environment.adminApiUrl}app/${appId}/model/${modelId}/statistics`)
    // return this.http.get(`${environment.adminApiUrl}app/${appId}/model/70462b86-0c23-4444-8ae5-611fa68f6be4/statistics`)
    return this.http.get(
      'http://localhost:4200/admin/app/cda81b8e-0c57-485f-a826-9e7c614bfdd9/model/a79212f6-23d9-4388-9e75-9315ee1c03d3/statistics'
    );
  }

  create(applicationId: string, name: string, type: string): Observable<Model> {
    name = name.trim();
    return this.http.post<Model>(`${environment.adminApiUrl}app/${applicationId}/model`, { name, type });
  }

  update(applicationId: string, modelId: string, name: string): Observable<Model> {
    name = name.trim();
    return this.http.put<Model>(`${environment.adminApiUrl}app/${applicationId}/model/${modelId}`, { name });
  }

  clone(applicationId: string, modelId: string, name: string): Observable<Model> {
    name = name.trim();
    return this.http.post<Model>(`${environment.adminApiUrl}app/${applicationId}/model/${modelId}`, { name });
  }

  delete(applicationId: string, modelId: string): Observable<Model> {
    return this.http.delete<Model>(`${environment.adminApiUrl}app/${applicationId}/model/${modelId}`);
  }
}
