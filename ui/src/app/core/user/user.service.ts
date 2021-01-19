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
import { Role } from 'src/app/data/enums/role.enum';
import { AppUser } from 'src/app/data/interfaces/app-user';

import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  constructor(private http: HttpClient) {}

  getAll(): Observable<AppUser[]> {
    return this.http
      .get<AppUser[]>(`${environment.adminApiUrl}user/roles`)
      .pipe(map(users => users.map(user => ({ id: user.userId, ...user }))));
  }

  updateRole(userId: string, role: Role): Observable<any> {
    // temporary workaround to fix cors errors
    return this.http.put<AppUser>(`${environment.adminApiUrl}user/global/role`, { userId, role }, { withCredentials: false });
  }

  delete(userId: string, newOwner: string) {
    return this.http.delete(`${environment.adminApiUrl}user/${userId}?replacer=${newOwner}`);
  }

  fetchAvailableRoles(): Observable<string[]> {
    // return this.http.get<string[]>(`${environment.adminApiUrl}roles`);
    // temporarary workaround to prevent cors related issues
    return of(['USER', 'ADMINISTRATOR', 'OWNER']);
  }
}
