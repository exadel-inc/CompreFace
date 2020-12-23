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
import { FormBuilder } from '@angular/forms';
import { Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { API } from '../../data/enums/api-url.enum';
import { Routes } from '../../data/enums/routers-url.enum';
import { AppState } from '../../store';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  refreshInProgress: boolean;
  requests = [];

  constructor(private http: HttpClient, private formBuilder: FormBuilder, private store: Store<AppState>, private router: Router) {}

  logIn(email: string, password: string): Observable<any> {
    const url = `${environment.adminApiUrl}${API.Login}`;
    const form = this.formBuilder.group({
      email,
      password,
      // eslint-disable-next-line @typescript-eslint/naming-convention
      grant_type: 'password',
    });
    const formData = new FormData();
    formData.append('username', form.get('email').value);
    formData.append('password', form.get('password').value);
    formData.append('grant_type', form.get('grant_type').value);

    // eslint-disable-next-line @typescript-eslint/naming-convention
    return this.http.post(url, formData, { headers: { Authorization: environment.basicToken }, withCredentials: false });
  }

  clearUserToken(): Observable<any> {
    const url = `${environment.adminApiUrl}${API.Login}`;

    // eslint-disable-next-line @typescript-eslint/naming-convention
    return this.http.delete(url, { headers: { Authorization: environment.basicToken } });
  }

  signUp(firstName: string, password: string, email: string, lastName: string): Observable<any> {
    const url = `${environment.adminApiUrl}${API.Register}`;
    return this.http.post(url, { email, password, firstName, lastName }, { observe: 'response' });
  }

  logOut() {
    this.clearUserToken();
    this.router.navigateByUrl(Routes.Login);
  }
}
