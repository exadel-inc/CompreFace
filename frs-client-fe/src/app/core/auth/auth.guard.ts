import { Injectable } from '@angular/core';
import { Router, CanActivate } from '@angular/router';

import { AuthService } from './auth.service';
import {ROUTERS_URL} from "../../data/routers-url.variable";


@Injectable()
export class AuthGuard implements CanActivate {

  constructor( public auth: AuthService,  public router: Router ) {}

  canActivate(): boolean {
    if (!this.auth.getToken()) {
      this.router.navigateByUrl(ROUTERS_URL.LOGIN);
      return false;
    }
    return true;
  }
}

@Injectable()
export class LoginGuard implements CanActivate {

  constructor( public auth: AuthService,  public router: Router ) {}

  canActivate(): boolean {
    if (this.auth.getToken()) {
      this.router.navigateByUrl(ROUTERS_URL.ORGANIZATION);
      return false;
    }
    return true;
  }
}
