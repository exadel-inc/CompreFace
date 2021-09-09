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
import { Observable, of } from 'rxjs';

import { environment } from '../../../environments/environment';
import { CollectionItem, SubjectExampleResponseItem } from 'src/app/data/interfaces/collection';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class CollectionService {
  constructor(private http: HttpClient) {}

  getSubjectsList(apiKey: string): Observable<{ subjects: string[] }> {
    return this.http.get<{ subjects: string[] }>(`${environment.userApiUrl}recognition/subjects`, { headers: { 'x-api-key': apiKey } });
  }

  addSubject(name: string, apiKey: string): Observable<{ subject: string }> {
    return this.http.post<{ subject: string }>(
      `${environment.userApiUrl}recognition/subjects`,
      { subject: name },
      { headers: { 'x-api-key': apiKey } }
    );
  }

  editSubject(editName: string, apiKey: string, subject: string): Observable<{ updated: boolean }> {
    return this.http.put<{ updated: boolean }>(
      `${environment.userApiUrl}recognition/subjects/${subject}`,
      { subject: editName },
      {
        headers: { 'x-api-key': apiKey },
      }
    );
  }

  deleteSubject(subject: string, apiKey: string): Observable<{ subject: string }> {
    return this.http.delete<{ subject: string }>(`${environment.userApiUrl}recognition/subjects/${subject}`, {
      headers: { 'x-api-key': apiKey },
    });
  }

  getSubjectExampleList(apiKey: string): Observable<SubjectExampleResponseItem[]> {
    return this.http
      .get(`${environment.userApiUrl}recognition/faces?size=1000`, {
        headers: { 'x-api-key': apiKey },
      })
      .pipe(map((resp: { faces: SubjectExampleResponseItem[] }) => resp.faces));
  }

  uploadSubjectExamples(item: CollectionItem, subject: string, apiKey: string): Observable<SubjectExampleResponseItem> {
    const { file } = item;
    const formData = new FormData();
    formData.append('file', file, file.name);

    return this.http.post<SubjectExampleResponseItem>(`${environment.userApiUrl}recognition/faces?subject=${subject}`, formData, {
      headers: { 'x-api-key': apiKey },
    });
  }

  deleteSubjectExample(item: CollectionItem, apiKey: string): Observable<SubjectExampleResponseItem> {
    return this.http.delete<SubjectExampleResponseItem>(`${environment.userApiUrl}recognition/faces/${item.id}`, {
      headers: { 'x-api-key': apiKey },
    });
  }

  deleteSubjectExamplesBulk(ids: string[], apiKey: string): Observable<SubjectExampleResponseItem[]> {
    return this.http.post<SubjectExampleResponseItem[]>(`${environment.userApiUrl}recognition/faces/delete`, ids, {
      headers: { 'x-api-key': apiKey },
    });
  }
}
