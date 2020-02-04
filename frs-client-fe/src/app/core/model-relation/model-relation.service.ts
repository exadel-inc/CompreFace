import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ModelRelation } from 'src/app/data/modelRelation';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ModelRelationService {

  constructor(private http: HttpClient) { }

  public getAll(organizationId: string, applicationId: string, modelId: string): Observable<ModelRelation[]> {
    return this.http.get<ModelRelation[]>(`${environment.apiUrl}org/${organizationId}/app/${applicationId}/model/${modelId}/apps`);
  }

  public update(organizationId: string, applicationId: string, modelId: string, id: string, shareMode: string): Observable<ModelRelation> {
    return this.http.put<ModelRelation>(`${environment.apiUrl}org/${organizationId}/app/${applicationId}/model/${modelId}/app`, { id, shareMode });
  }
}
