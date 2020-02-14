import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, BehaviorSubject} from 'rxjs';
import {environment} from '../../../environments/environment';
import {API_URL} from '../../data/api.variables';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  public token$: BehaviorSubject<string>;

  constructor(private http: HttpClient) {
    this.token$ = new BehaviorSubject<string>(localStorage.getItem('token'));
  }

  getToken(): string {
    return this.token$.getValue();
  }

  updateToken(token: string): void {
    this.token$.next(token);
    localStorage.setItem('token', token);
  }

  removeToken(): void {
    this.token$.next(null);
    localStorage.removeItem('token');
  }

  logIn(email: string, password: string): Observable<any> {
    const url = `${environment.apiUrl}${API_URL.LOGIN}`;
    return this.http.post(url, { email, password });
  }

  signUp(firstName: string, password: string, email: string, lasName: string): Observable<any> {
    const url = `${environment.apiUrl}${API_URL.REGISTER}`;
    return this.http.post(url, { email, password, firstName, lasName });
  }

  logOut(token: string): Observable<any> {
    const url = `${environment.apiUrl}${API_URL.LOGOUT}`;
    return this.http.post(url, { token });
  }

  // todo: for feature
  isTokenValid(token: string): boolean {
    return true;
  }
}
