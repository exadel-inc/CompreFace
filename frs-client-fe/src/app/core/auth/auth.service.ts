import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {environment} from "../../../environments/environment";
import { API_URL } from "../../data/api.variables";
import {User} from "../../data/user";

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  constructor(private http: HttpClient) {
  }

  getToken(): string {
    return localStorage.getItem('token') || '';
  }

  logIn(username: string, password: string): Observable<any> {
    const url = `${environment.apiUrl}${API_URL.LOGIN}`;
    return this.http.post<User>(url, {username, password});
  }

  signUp(username: string, password: string, email: string): Observable<any> {
    const url = `${environment.apiUrl}${API_URL.REGISTER}`;
    return this.http.post<User>(url, {email, password, username});
  }

  // todo: for feature
  logOut(token: string): Observable<any> {
    const url = `${environment.apiUrl}${API_URL.LOGOUT}`;
    return this.http.post(url, {token});
  }
}
