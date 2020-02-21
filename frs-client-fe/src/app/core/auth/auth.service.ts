import {Injectable} from '@angular/core';
import {HttpClient, HttpEvent, HttpHeaders} from '@angular/common/http';
import {Observable, BehaviorSubject} from 'rxjs';
import {environment} from '../../../environments/environment';
import {API_URL} from '../../data/api.variables';
import {FormBuilder} from '@angular/forms';
import {catchError, flatMap, tap} from 'rxjs/operators';

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

  getRefreshToken(): string {
    return localStorage.getItem('refreshToken');
  }

  updateTokens(token: string, refreshToken: string): void {
    this.token$.next(`Bearer ${token}`);
    localStorage.setItem('token', `Bearer ${token}`);
    localStorage.setItem('refreshToken', refreshToken);
  }

  removeToken(): void {
    this.token$.next(null);
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
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

  refreshToken(req, next): Observable<HttpEvent<any>> {
    const url = `${environment.apiUrl}${API_URL.REFRESH_TOKEN}`;

    const form = this.formBuilder.group({
      grant_type: 'refresh_token',
      refresh_token: this.getRefreshToken()
    });
    const formData = new FormData();
    formData.append('grant_type', form.get('grant_type').value);
    formData.append('refresh_token', form.get('refresh_token').value);

    return this.http.post(url, formData, { headers: { Authorization: environment.basicToken } }).pipe(
      tap(e => console.log(e)),
      flatMap(
        (data: any): Observable<HttpEvent<any>> => {
          console.log(data);
          if (data.access_token && data.refresh_token) {
            this.updateTokens(data.access_token, data.refresh_token);
            console.log(data);
            req = req.clone({
              setHeaders: {
                Authorization: data.access_token,
              }
            });
            console.log('req', req);
            return next.handle(req).catchError(e => console.log(e));
          } else {
            // Logout from account
          }
        }
      )
    );
  }

  // todo: for feature
  isTokenValid(token: string): boolean {
    return true;
  }
}
