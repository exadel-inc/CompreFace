import { Injectable } from '@angular/core';
import { Router, CanActivate } from '@angular/router';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { AuthService } from './auth.service';
import {ROUTERS_URL} from "../../data/routers-url.variable";


@Injectable()
export class AuthGuard implements CanActivate {

  constructor( public auth: AuthService,  public router: Router ) {}

  canActivate(): Observable<boolean> {
      return this.auth.getToken().pipe(map(tokenValue => {
          const isAuthorised = !!tokenValue;

          if (!isAuthorised) {
            this.router.navigateByUrl(ROUTERS_URL.LOGIN);
          }

          return isAuthorised;
      }));
  }
}

@Injectable()
export class LoginGuard implements CanActivate {

  constructor( public auth: AuthService,  public router: Router ) {}

  canActivate(): Observable<boolean> {
    return this.auth.getToken().pipe(map(tokenValue => {
        const isAuthorised = !!tokenValue;

        if (isAuthorised) {
          this.router.navigateByUrl(ROUTERS_URL.ORGANIZATION);
        }

        return !isAuthorised;
    }));
  }
}
