import {Injectable, Injector} from '@angular/core';
import {HttpEvent, HttpInterceptor, HttpHandler, HttpRequest, HttpErrorResponse} from '@angular/common/http';
import {AuthService} from './auth.service';
import {Observable, throwError} from 'rxjs';
import {catchError} from 'rxjs/operators';

@Injectable()
export class TokenInterceptor implements HttpInterceptor {
  private authService: AuthService;

  constructor(private injector: Injector) {
    this.authService = this.injector.get(AuthService);
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.authService.getToken();
    if (token) {
      request = request.clone({
        setHeaders: {
          Authorization: token,
        }
      });
    }

    return next.handle(request);
  }
}

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  private authService: AuthService;

  constructor(private injector: Injector) {
    this.authService = this.injector.get(AuthService);
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

    return next.handle(request).pipe(
      catchError((response: any): Observable<HttpEvent<any>> => {
        if (response instanceof HttpErrorResponse && response.status === 401) {
          if (response.error.message === 'Error during authentication') {
            console.log('refreshToken run');
            return this.authService.refreshToken(request);
          } else {
            this.authService.logOut();
          }
        }

        return throwError(response);
      })
    );
  }
}
