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

  recognize(file: any, apiKey: string): Observable<any> {
    const url = `${environment.userApiUrl}faces/recognize`;
    const formData = new FormData();
    formData.append('file', file);

    return  this.http.post(url, formData, {
      headers: { 'x-api-key': apiKey}
    }).pipe(
      map((data) => ({
        data,
        request: this.createUIRequest(url, { 'x-api-key': apiKey})
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

  /**
   * Create mocked request just to display request info on UI.
   *
   * @param url Request url.
   * @param options Headers options.
   * @param params url parameters.
   * @private
   */
  private createUIRequest(url: string,  options = {}, params = {}): any {
    const parsedParams = Object.keys(params).length ? `?${(new URLSearchParams(params)).toString()}` : '';

    return {
      Headers: {
        'Content-Type': 'multipart/form-data',
        Origin: window.location.origin,
        Referer: `${window.location.origin}${parsedParams}`,
        host: `${window.location.host}:${window.location.port}`,
        ...options
      }
    };
  }
}
