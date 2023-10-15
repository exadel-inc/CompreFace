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
import { SignUp } from '../../data/interfaces/sign-up';
import { shareReplay, tap } from 'rxjs/operators';
import { selectUserId } from 'src/app/store/userInfo/selectors';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  refreshInProgress: boolean;
  requests = [];
  currentUserId$: Observable<string | null>;

  constructor(private http: HttpClient, private formBuilder: FormBuilder, private store: Store<AppState>, private router: Router) {
    this.currentUserId$ = this.store.select(selectUserId).pipe(shareReplay(1));
  }

  logIn(email: string, password: string, grant_type: string): Observable<any> {
    const url = `${environment.adminApiUrl}${API.Login}`;
    const form = this.formBuilder.group({
      email,
      password,
      // eslint-disable-next-line @typescript-eslint/naming-convention
      grant_type,
    });
    const formData = new FormData();
    formData.append('username', form.get('email').value);
    formData.append('password', form.get('password').value);
    formData.append('grant_type', form.get('grant_type').value);

    // eslint-disable-next-line @typescript-eslint/naming-convention
    return this.http.post(url, formData, { headers: { Authorization: environment.basicToken }, withCredentials: false });
  }

  refreshToken(grant_type: string): Observable<any> {
    const url = `${environment.adminApiUrl}${API.Login}?grant_type=${grant_type}&scope=all`;
    const form = this.formBuilder.group({
      scope: 'all',
      grant_type,
    });
    const formData = new FormData();
    formData.append('grant_type', form.get('grant_type').value);
    formData.append('scope', form.get('scope').value);
    return this.http.post(url, formData, { headers: { Authorization: environment.basicToken }, withCredentials: false });
  }

  clearUserToken(): Observable<any> {
    const url = `${environment.adminApiUrl}${API.Login}`;

    // eslint-disable-next-line @typescript-eslint/naming-convention
    return this.http.delete(url, { headers: { Authorization: environment.basicToken } });
  }

  signUp(firstName: string, password: string, email: string, lastName: string, isAllowStatistics?: boolean): Observable<any> {
    const url = `${environment.adminApiUrl}${API.Register}`;
    const body: SignUp = { email, password, firstName, lastName };

    if (isAllowStatistics !== undefined) {
      body.isAllowStatistics = isAllowStatistics;
    }
    return this.http.post(url, body, { observe: 'response' });
  }

  logOut(): void {
    const url: string = this.router.url;
    const queryParam = url === Routes.Login ? {} : { queryParams: { redirect: url } };

    this.clearUserToken();
    this.router.navigate([Routes.Login], { ...queryParam });
  }

  navigateToLogin(): void {
    this.router.navigate([Routes.Login]);
  }

  changePassword(oldPassword: string, newPassword: string): Observable<any> {
    const url = `${environment.adminApiUrl}${API.ChangePassword}`;
    return this.http.put(url, { oldPassword, newPassword }, { observe: 'response' });
  }

  recoveryPassword(email: string): Observable<any> {
    const url = `${environment.adminApiUrl}${API.ForgotPassword}`;
    return this.http.post(url, { email: email });
  }

  updatePassword(password: string, token: string): Observable<any> {
    const url = `${environment.adminApiUrl}${API.ResetPassword}?token=${token}`;

    return this.http.put(url, { password: password }, { observe: 'response' });
  }
}
