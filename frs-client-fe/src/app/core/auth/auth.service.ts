import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {User} from "../../data/user";
import {environment} from "../../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  constructor(private http: HttpClient) {
  }

  getToken(): string {
    return localStorage.getItem('token');
  }

  logIn(username: string, password: string): Observable<any> {
    const url = `${environment.apiUrl}/admin/oauth/token`;
    return this.http.post<User>(url, {username, password});
  }

  signUp(username: string, password: string, email: string): Observable<User> {
    const url = `${environment.apiUrl}/admin/client/register`;
    return this.http.post(url, {email, password, username});
  }
}
