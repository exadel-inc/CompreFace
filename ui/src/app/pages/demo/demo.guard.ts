import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Store } from '@ngrx/store';
import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot,
  RouterStateSnapshot, UrlTree, Router } from '@angular/router';
import { selectDemoPageAvailability } from '../../store/demo/selectors';
import { ROUTERS_URL } from '../../data/enums/routers-url.enum';

@Injectable({
  providedIn: 'root'
})
export class DemoGuard implements CanActivate {
  constructor(private store: Store<any>, private router: Router) {
  }

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    return this.store.select(selectDemoPageAvailability).pipe(
      tap((hasAccess) => {
        if (!hasAccess) {
          this.router.navigate([ROUTERS_URL.HOME]);
        }
      })
    );
  }
}
