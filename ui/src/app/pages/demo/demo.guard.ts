import { Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { Store } from '@ngrx/store';
import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot,
  RouterStateSnapshot, UrlTree, Router } from '@angular/router';
import { ROUTERS_URL } from '../../data/enums/routers-url.enum';
import { DemoService } from './demo.service';
import { loadDemoApiKeyAction } from '../../store/demo/actions';

@Injectable({
  providedIn: 'root'
})
export class DemoGuard implements CanActivate {
  constructor(private store: Store<any>, private router: Router, private demoService: DemoService) {
  }

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    return this.demoService.getModel().pipe(
      map(() => true),
      catchError(() => of(false)),
      tap((hasAccess) => {
        if (!hasAccess) {
          this.router.navigate([ROUTERS_URL.HOME]);
        } else {
          this.store.dispatch(loadDemoApiKeyAction());
        }
      })
    );
  }
}
