import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Model } from '../../data/model';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

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
