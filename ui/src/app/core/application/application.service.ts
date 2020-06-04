import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Application } from 'src/app/data/application';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ApplicationService {

  constructor(private http: HttpClient) { }

  getAll(organizationId: string): Observable<Application[]> {
    return this.http.get<Application[]>(`${environment.apiUrl}org/${organizationId}/apps`);
  }

  create(organizationId: string, name: string): Observable<Application> {
    return this.http.post<Application>(`${environment.apiUrl}org/${organizationId}/app`, { name });
  }

  put(organizationId: string, appId: string, name: string): Observable<Application> {
    return this.http.put<Application>(`${environment.apiUrl}org/${organizationId}/app/${appId}`, { name });
  }

  delete(organizationId: string, appId: string) {
    return this.http.delete(`${environment.apiUrl}org/${organizationId}/app/${appId}`);
  }
}
