import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from 'src/environments/environment';
import {Organization} from '../../data/organization';

@Injectable({
  providedIn: 'root'
})
export class OrganizationUtilsService {
  constructor(
    private http: HttpClient,
  ) { }

  public create(name: string): Observable<Organization> {
    return this.http.post<Organization>(`${environment.apiUrl}org`, { name });
  }
}
