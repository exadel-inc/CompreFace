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

import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { environment } from '../../../environments/environment';
import { Model } from '../../data/interfaces/model';
import { UIRequestOptions } from '../../data/interfaces/ui-request-options';
import { UIDoubleFileRequestOptions } from '../../data/interfaces/ui-double-request-options';
import { RequestResultVerification } from '../../data/interfaces/response-result';

@Injectable({
  providedIn: 'root',
})
export class FaceRecognitionService {
  headers: HttpHeaders;

  constructor(private http: HttpClient) { }

  addFace(file: any, model: Model): Observable<any> {
    const formData = new FormData();
    formData.append('file', file, file.name);
    formData.append('subject', file.name);
    return this.http.post(`${environment.userApiUrl}recognition/faces`, formData, {
      headers: { 'x-api-key': model.apiKey },
    });
  }

  recognize(file: any, apiKey: string, landmarks: string): Observable<any> {
    const url = `${environment.userApiUrl}recognition/recognize`;
    const formData = new FormData();
    formData.append('file', file);

    return this.http
      .post(url, formData, {
        headers: { 'x-api-key': apiKey },
        // eslint-disable-next-line @typescript-eslint/naming-convention
        params: { face_plugins: [landmarks, 'gender', 'age', 'pose'] },
      })
      .pipe(
        map(data => ({
          data,
          request: this.createUIRequest(url, { apiKey, file }),
        }))
      );
  }

  detection(file: any, apiKey: string, landmarks: string): Observable<any> {
    const url = `${environment.userApiUrl}detection/detect`;
    const formData = new FormData();
    formData.append('file', file);

    return this.http
      .post(url, formData, {
        headers: { 'x-api-key': apiKey },
        // eslint-disable-next-line @typescript-eslint/naming-convention
        params: { face_plugins: [landmarks, 'gender', 'age', 'pose'] },
      })
      .pipe(
        map(data => ({
          data,
          request: this.createUIRequest(url, { apiKey, file }),
        }))
      );
  }

  verification(
    sourceImage: File,
    targetImage: File,
    apiKey: string,
    landmarks: string
  ): Observable<{ data: { result: RequestResultVerification }; request: string }> {
    const url = `${environment.userApiUrl}verification/verify`;
    const formData: FormData = new FormData();

    formData.append('source_image', sourceImage);
    formData.append('target_image', targetImage);

    return this.http
      .post(url, formData, {
        headers: { 'x-api-key': apiKey },
        // eslint-disable-next-line @typescript-eslint/naming-convention
        params: { face_plugins: [landmarks, 'gender', 'age', 'pose'] },
      })
      .pipe(
        map(data => data as { result: RequestResultVerification }),
        map(data => ({
          data,
          request: this.createUIDoubleFileRequest(url, { apiKey, sourceImage, targetImage }),
        }))
      );
  }

  getAllFaces(model: Model): Observable<any> {
    return this.http.get(`${environment.userApiUrl}recognition/faces`, { headers: { 'x-api-key': model.apiKey } });
  }

  train(model: Model): Observable<any> {
    const formData = new FormData();
    return this.http.post(`${environment.userApiUrl}retrain`, formData, { headers: { 'x-api-key': model.apiKey } });
  }

  /**
   * Create mocked request just to display request info on UI.
   *
   * @param url Request url.
   * @param options Headers options.
   * @param params url parameters.
   * @private
   */
  private createUIRequest(url: string, options = {} as UIRequestOptions, params = {}): string {
    const {
      apiKey,
      file: { name: fname },
    } = options;
    return `curl -X POST "${window.location.origin}${url}?face_plugins=landmarks, gender, age, pose" \\\n-H "Content-Type: multipart/form-data" \\\n-H "x-api-key: ${apiKey}" \\\n-F "file=@${fname}"`;
  }

  private createUIDoubleFileRequest(url: string, options = {} as UIDoubleFileRequestOptions, params = {}): string {
    const {
      apiKey,
      sourceImage: { name: ffname },
      targetImage: { name: sfname },
    } = options;
    return `curl -X POST "${window.location.origin}${url}?face_plugins=landmarks, gender, age, pose" \\\n-H "Content-Type: multipart/form-data" \\\n-H "x-api-key: ${apiKey}" \\\n-F "source_image=@${ffname}" \\\n-F "target_image=@${sfname}"`;
  }
}
