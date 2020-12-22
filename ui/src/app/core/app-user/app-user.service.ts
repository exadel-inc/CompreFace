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
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Role } from 'src/app/data/enums/role.enum';
import { AppUser } from 'src/app/data/interfaces/app-user';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root',
})
export class AppUserService {
  constructor(private http: HttpClient) {}

  getAll(applicationId: string): Observable<AppUser[]> {
    return this.http
      .get<AppUser[]>(`${environment.adminApiUrl}app/${applicationId}/roles`)
      .pipe(map(users => users.map(user => ({ id: user.userId, ...user }))));
  }

  update(applicationId: string, userId: string, role: Role): Observable<AppUser> {
    return this.http.put<AppUser>(`${environment.adminApiUrl}app/${applicationId}/role`, { userId, role });
  }

  inviteUser(applicationId: string, userEmail: string, role: Role): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${environment.adminApiUrl}app/${applicationId}/invite`, {
      userEmail,
      role,
    });
  }

  deleteUser(applicationId: string, userId: string) {
    return this.http.delete(`${environment.adminApiUrl}app/${applicationId}/user/${userId}`);
  }
}
