import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Application } from 'src/app/data/application';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ModelRelationService {

  constructor(private http: HttpClient) { }

  public getAll(organizationId: string, applicationId: string, modelId: string): Observable<Application[]> {
    return this.http.get<Application[]>(`${environment.apiUrl}org/${organizationId}/app/${applicationId}/model/${modelId}/apps`);
  }

  public update(organizationId: string, applicationId: string, modelId: string, id: string, role: string): Observable<Application> {
    return this.http.put<Application>(`${environment.apiUrl}org/${organizationId}/app/${applicationId}/model/${modelId}/app`, { id, role });
  }
}
