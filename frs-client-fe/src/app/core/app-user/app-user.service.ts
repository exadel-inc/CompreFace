import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {AppUser} from 'src/app/data/appUser';
import {environment} from 'src/environments/environment';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AppUserService {

  constructor(private http: HttpClient) { }

  public getAll(organizationId: string, applicationId: string): Observable<AppUser[]> {
    return this.http.get<AppUser[]>(`${environment.apiUrl}org/${organizationId}/app/${applicationId}/roles`);
  }

  public update(organizationId: string, applicationId: string, id: string, role: string): Observable<AppUser> {
    return this.http.put<AppUser>(`${environment.apiUrl}org/${organizationId}/app/${applicationId}/role`, { id, role });
  }

  public inviteUser(organizationId: string, applicationId: string, userEmail: string, role: string): Observable<{message: string}> {
    return this.http.put<{message: string}>(`${environment.apiUrl}org/${organizationId}/app/${applicationId}/invite`, { userEmail, role });
  }
}
