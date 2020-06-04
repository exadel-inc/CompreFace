import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { AppUser } from 'src/app/data/appUser';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AppUserService {

  constructor(private http: HttpClient) { }

  getAll(organizationId: string, applicationId: string): Observable<AppUser[]> {
    return this.http.get<AppUser[]>(`${environment.apiUrl}org/${organizationId}/app/${applicationId}/roles`)
      .pipe(
        map(users => users.map(user => ({ id: user.userId, ...user })))
      );
  }

  update(organizationId: string, applicationId: string, userId: string, role: string): Observable<AppUser> {
    return this.http.put<AppUser>(`${environment.apiUrl}org/${organizationId}/app/${applicationId}/role`, { userId, role });
  }

  inviteUser(organizationId: string, applicationId: string, userEmail: string, role: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${environment.apiUrl}org/${organizationId}/app/${applicationId}/invite`,
      { userEmail, role });
  }

  deleteUser(organizationId: string, applicationId: string, userId: string) {
    return this.http.delete(`${environment.apiUrl}org/${organizationId}/app/${applicationId}/user/${userId}`);
  }
}
