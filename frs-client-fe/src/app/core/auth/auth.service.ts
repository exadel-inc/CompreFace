import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable, BehaviorSubject} from 'rxjs';
import {environment} from '../../../environments/environment';
import {API_URL} from '../../data/api.variables';
import {FormBuilder} from '@angular/forms';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  public token$: BehaviorSubject<string>;

  constructor(private http: HttpClient, private formBuilder: FormBuilder) {
    this.token$ = new BehaviorSubject<string>(localStorage.getItem('token'));
  }

  getToken(): string {
    return this.token$.getValue();
  }

  updateToken(token: string): void {
    this.token$.next(`Bearer ${token}`);
    localStorage.setItem('token', `Bearer ${token}`);
  }

  removeToken(): void {
    this.token$.next(null);
    localStorage.removeItem('token');
  }

  logIn(email: string, password: string): Observable<any> {
    const url = `${environment.apiUrl}${API_URL.LOGIN}`;
    const form = this.formBuilder.group({
      email,
      password,
      grant_type: 'password'
    });
    const formData = new FormData();
    formData.append('username', form.get('email').value);
    formData.append('password',  form.get('password').value);
    formData.append('grant_type', form.get('grant_type').value);
    return this.http.post(url, formData, { headers: { Authorization: environment.basicToken } });
  }

  signUp(firstName: string, password: string, email: string, lastName: string): Observable<any> {
    const url = `${environment.apiUrl}${API_URL.REGISTER}`;
    return this.http.post(url, { email, password, firstName, lastName });
  }

  logOut(token: string): Observable<any> {
    const url = `${environment.apiUrl}${API_URL.LOGOUT}`;
    return this.http.post(url, {token});
  }

  // todo: for feature
  isTokenValid(token: string): boolean {
    return true;
  }
}
