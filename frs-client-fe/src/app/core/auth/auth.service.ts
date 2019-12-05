import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {User} from "../../data/user";

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private BASE_URL = 'http://localhost:3000';

  constructor(private http: HttpClient) {
  }

  getToken(): string {
    return localStorage.getItem('token');
  }

  logIn(username: string, password: string): Observable<any> {
    const url = `${this.BASE_URL}/admin/oauth/token`;
    return this.http.post<User>(url, {username, password});
  }

  signUp(username: string, password: string, email: string): Observable<User> {
    const url = `${this.BASE_URL}/admin/client/register`;
    return this.http.post(url, {email, password, username});
  }
}
