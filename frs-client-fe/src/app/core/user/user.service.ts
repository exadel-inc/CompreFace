import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import {AppUser} from 'src/app/data/appUser';
import {environment} from '../../../environments/environment';
import {map, catchError} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  constructor(private http: HttpClient) { }

  public getAll(organizationId: string): Observable<AppUser[]> {
    return this.http.get<AppUser[]>(`${environment.apiUrl}org/${organizationId}/roles`).pipe(
      map(users => users.map(user => ({id: user.userId, ...user})))
    );
  }

  public updateRole(organizationId: string, id: string, role: string): Observable<any> {
    // temporary workaround to fix cors errors
    return this.http.put<AppUser>(`${environment.apiUrl}org/${organizationId}/role`, { id, role }, {withCredentials: false});
  }

  public inviteUser(organizationId: string, userEmail: string, role: string): Observable<{message: string}> {
    return this.http.put<{message: string}>(`${environment.apiUrl}org/${organizationId}/invite`, { userEmail, role });
  }

  public fetchAvailableRoles(): Observable<string[]> {
    // return this.http.get<string[]>(`${environment.apiUrl}roles`);
    // temporarary workaround to prevent cors related issues
    return of([
      'OWNER',
      'ADMINISTRATOR',
      'USER'
    ]);
  }
}
