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

import { environment } from '../../../environments/environment';
import { API } from '../../data/enums/api-url.enum';
import { User } from '../../data/interfaces/user';
import { UserInfo } from '../../data/interfaces/user-info';

@Injectable({
  providedIn: 'root',
})
export class UserInfoService {
  constructor(private http: HttpClient) {}

  get(): Observable<User> {
    return this.http.get<User>(`${environment.adminApiUrl}${API.UserInfo}`);
  }

  editUserInfo(firstName: string, lastName: string): Observable<UserInfo> {
    return this.http.put<UserInfo>(`${environment.adminApiUrl}user/update`, { firstName, lastName });
  }
}
