import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AppUser } from 'src/app/data/appUser';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  constructor(private http: HttpClient) { }

  public getAll(organizationId: string): Observable<AppUser[]> {
    return this.http.get<AppUser[]>(`${environment.apiUrl}org/${organizationId}/roles`);
  }

  public updateRole(organizationId: string, id: string, role: string): Observable<any> {
    return this.http.post<AppUser>(`${environment.apiUrl}org/${organizationId}/role`, { id, role });
  }

  public inviteUser(organizationId: string, role: string, userEmail: string): Observable<{message: string}> {
    return this.http.post<{message: string}>(`${environment.apiUrl}org/${organizationId}/invite`, { role, userEmail });
  }

  public fetchAvailableRoles(): Observable<string[]> {
    return this.http.get<string[]>(`${environment.apiUrl}roles`);
  }
}
