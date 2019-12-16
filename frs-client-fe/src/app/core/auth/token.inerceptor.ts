import { Injectable, Injector } from '@angular/core';
import { HttpEvent, HttpInterceptor, HttpHandler, HttpRequest, HttpErrorResponse } from '@angular/common/http';
import { AuthService } from './auth.service';
import { Observable, of } from "rxjs";
import { concatMap } from "rxjs/operators";
import { Router } from "@angular/router";
import { catchError } from "rxjs/operators";


@Injectable()
export class TokenInterceptor implements HttpInterceptor {
    private authService: AuthService;

    constructor(private injector: Injector) {
        this.authService = this.injector.get(AuthService);
    }

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        return this.authService.getToken()
            .pipe(
                concatMap(tokenValue => {
                    request.clone({
                        setHeaders: {
                            'Authorization': tokenValue,
                        }
                    });

                    return next.handle(request);
                })
            );
    }
}

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
    private authService: AuthService;

    constructor(private router: Router, private injector: Injector) {
        this.authService = this.injector.get(AuthService);
    }

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

        return next.handle(request).pipe(
            catchError((response: any) => {
                if (response instanceof HttpErrorResponse && response.status === 401) {
                    this.authService.removeToken();
                    this.router.navigateByUrl('/login');
                }

                return Observable.throw(response);
            })
        )
    }
}
