import {Injectable, Injector} from '@angular/core';
import {HttpEvent, HttpInterceptor, HttpHandler, HttpRequest, HttpErrorResponse} from '@angular/common/http';
import {AuthService} from './auth.service';
import {Observable, throwError} from 'rxjs';
import {Router} from '@angular/router';
import {catchError} from 'rxjs/operators';
import {Store} from '@ngrx/store';
import {AppState} from 'src/app/store';
import {updateUserAuthorization} from '../../store/userInfo/action';
import {ROUTERS_URL} from '../../data/routers-url.variable';


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

  constructor(private router: Router, private injector: Injector, private store: Store<AppState>) {
    this.authService = this.injector.get(AuthService);
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

    return next.handle(request).pipe(
      catchError((response: any) => {
        if (response instanceof HttpErrorResponse && response.status === 401) {
          this.authService.removeToken();
          this.store.dispatch(updateUserAuthorization({ value: false }));
          this.router.navigateByUrl(ROUTERS_URL.LOGIN);
        }

        return throwError(response);
      })
    );
  }
}
