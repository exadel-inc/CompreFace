import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Model } from '../../data/interfaces/model';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class FaceRecognitionService {
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

  recognize(file: any, model: Model): Observable<any> {
    const url = `${environment.userApiUrl}recognize`;
    const formData = new FormData();
    formData.append('file', file);

    const request = this.http.post(url, formData, {
      headers: { 'x-api-key': model.apiKey}
    });

    return request.pipe(
      map((data) => ({
        data,
        request: this.createUIRequest(url, { 'x-api-key': model.apiKey})
      }))
    );
  }

  getAllFaces(model: Model): Observable<object> {
    return this.http.get(`${environment.userApiUrl}faces`, { headers: { 'x-api-key': model.apiKey }});
  }

  train(model: Model): Observable<object> {
    const formData = new FormData();
    return this.http.post(`${environment.userApiUrl}retrain`, formData, { headers: { 'x-api-key': model.apiKey }});
  }

  private createUIRequest(url: string,  options = {}, params = {}): any {
    const parsedParams = Object.keys(params).length ? `?${(new URLSearchParams(params)).toString()}` : '';

    return {
      'Request Headers': {
        'Content-Type': 'multipart/form-data',
        Accept: ': application/json, text/plain, */*',
        'Accept-Encoding': 'gzip, deflate, br',
        'Accept-Language': 'pl,uk-UA;q=0.9,uk;q=0.8,en-US;q=0.7,en;q=0.6',
        Connection: 'keep-alive',
        Origin: window.location.origin,
        Referer: `${window.location.origin}${parsedParams}`,
        host: `${window.location.host}:${window.location.port}`,
        'User-Agent': window.navigator.userAgent,
        ...options
      },
      'Request Body': 'File...'
    };
  }
}

// Accept: application/json, text/plain, */*
// Accept-Encoding: gzip, deflate, br
// Accept-Language: pl,uk-UA;q=0.9,uk;q=0.8,en-US;q=0.7,en;q=0.6
// Authorization: Bearer 1244a2ed-c364-4c61-9100-437c539ffde2
// Connection: keep-alive
// Content-Length: 6603
// Content-Type: multipart/form-data; boundary=----WebKitFormBoundaryQgtV9Qg5eKK3fe9d
// Host: localhost:4200
// Origin: http://localhost:4200
// Referer: http://localhost:4200/test-model?org=00000000-0000-0000-0000-000000000000&app=9841c167-f023-4c8f-96fe-7bcb9e4f7ebb&model=05630e59-69b3-48a3-a4fe-c3f4ce2e61db
// Sec-Fetch-Dest: empty
// Sec-Fetch-Mode: cors
// Sec-Fetch-Site: same-origin
// User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.80 Safari/537.36
// x-api-key: 18044bed-74f1-49fc-a567-9f37e5300c11














