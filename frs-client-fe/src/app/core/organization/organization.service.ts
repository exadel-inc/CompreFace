import { Injectable } from '@angular/core';
import {Observable} from "rxjs";
import {environment} from "../../../environments/environment";
import {API_URL} from "../../data/api.variables";
import {HttpClient} from "@angular/common/http";
import {Organization} from "../../data/organization";

@Injectable({
  providedIn: 'root'
})
export class OrganizationService {

  constructor(private http: HttpClient) { }

  GetAll(): Observable<any> {
    const url = `${environment.apiUrl}${API_URL.ORGANIZATIONS}`;
    return this.http.get<[Organization]>(url);
  }

  Create(organization: Organization): Observable<any> {
    const url = `${environment.apiUrl}${API_URL.ORGANIZATIONS}`;
    return this.http.post<[Organization]>(url, organization);
  }

  GetOne(id: string): Observable<any> {
    const url = `${environment.apiUrl}${API_URL.ORGANIZATIONS}/${id}`;
    return this.http.get<[Organization]>(url);
  }

  UpdateOne(organization: Organization): Observable<any> {
    const { id } = organization;
    const url = `${environment.apiUrl}${API_URL.ORGANIZATIONS}/${id}`;
    return this.http.put<[Organization]>(url, { organization });
  }

}
