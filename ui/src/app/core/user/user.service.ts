/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { AppUser} from 'src/app/data/appUser';

import { environment } from '../../../environments/environment';
import { RoleEnum } from 'src/app/data/roleEnum.enum';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  constructor(private http: HttpClient) { }

  getAll(organizationId: string): Observable<AppUser[]> {
    return this.http.get<AppUser[]>(`${environment.apiUrl}org/${organizationId}/roles`).pipe(
      map(users => users.map(user => ({ id: user.userId, ...user })))
    );
  }

  updateRole(organizationId: string, userId: string, role: RoleEnum): Observable<any> {
    // temporary workaround to fix cors errors
    return this.http.put<AppUser>(`${environment.apiUrl}org/${organizationId}/role`, { userId, role }, { withCredentials: false });
  }

  delete(organizationId: string, userId: string, newOwner?: string) {
    return this.http.delete(`${environment.apiUrl}user/${userId}?replacer=${newOwner}`);
  }

  fetchAvailableRoles(): Observable<string[]> {
    // return this.http.get<string[]>(`${environment.apiUrl}roles`);
    // temporarary workaround to prevent cors related issues
    return of([
      'USER',
      'ADMINISTRATOR',
      'OWNER'
    ]);
  }
}
