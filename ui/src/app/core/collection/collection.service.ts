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
import { CollectionItem, SubjectExampleResponseItem } from 'src/app/data/interfaces/collection';
import { map } from 'rxjs/operators';
import { CollectionInfo } from 'src/app/data/interfaces/collection-info';

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
    const subjectEncoded = encodeURIComponent(subject);
    return this.http.put<{ updated: boolean }>(
      `${environment.userApiUrl}recognition/subjects/${subjectEncoded}`,
      { subject: editName },
      {
        headers: { 'x-api-key': apiKey },
      }
    );
  }

  deleteSubject(subject: string, apiKey: string): Observable<{ subject: string }> {
    const subjectEncoded = encodeURIComponent(subject);
    return this.http.delete<{ subject: string }>(`${environment.userApiUrl}recognition/subjects/${subjectEncoded}`, {
      headers: { 'x-api-key': apiKey },
    });
  }

  getSubjectMediaNextPage(apiKey: string, subject: string, next: number): Observable<SubjectExampleResponseItem[]> {
    const pageSize = 15;
    return this.getSubjectMedia(apiKey, subject, next, pageSize);
  }

  getSubjectMedia(apiKey: string, subject: string, page: number = 0, size: number = 15) {
    const subjectEncoded = encodeURIComponent(subject);
    return this.http
      .get(`${environment.userApiUrl}recognition/faces?size=${size}&subject=${subjectEncoded}&page=${page}`, {
        headers: { 'x-api-key': apiKey },
      })
      .pipe(
        map((resp: CollectionInfo) => {
          const totalPages = resp.total_pages;
          const totalElements = resp.total_elements;
          return resp.faces.map(el => ({ ...el, page: page, totalPages: totalPages, totalElements: totalElements }));
        })
      );
  }

  getTotalImagesInfo(apiKey: string): Observable<number> {
    return this.http
      .get(`${environment.userApiUrl}recognition/faces`, {
        headers: { 'x-api-key': apiKey },
      })
      .pipe(map((resp: CollectionInfo) => resp.total_elements));
  }

  uploadSubjectExamples(item: CollectionItem, subject: string, apiKey: string): Observable<SubjectExampleResponseItem> {
    const { file } = item;
    const formData = new FormData();
    formData.append('file', file, file.name);
    const subjectEncoded = encodeURIComponent(subject);

    return this.http.post<SubjectExampleResponseItem>(`${environment.userApiUrl}recognition/faces?subject=${subjectEncoded}`, formData, {
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
